(ns try-lwjgl.models
  (:require [try-lwjgl.physics :as physics]
            [try-lwjgl.model.axes :as axes]
            [try-lwjgl.model.grid :as grid]
            [try-lwjgl.model.container-cube :as container-cube]
            [try-lwjgl.model.ground :as model.ground]
            [try-lwjgl.model.block :as model.block]
            [try-lwjgl.model.ball :as model.ball]
            [try-lwjgl.model.player :as model.player]
            [try-lwjgl.model.highlight :as highlight]))

(def world  (atom nil))
(def ball   (atom nil))
(def ground (atom nil))
(def player (atom nil))
(def blocks (atom nil))

(defn simulate [delta]
  (.stepSimulation @world (* delta 1000.0)))

(defn draw []
  (axes/draw)
  ;(grid/draw)
  (container-cube/draw)
  (model.ground/draw @ground)
  (model.block/draw-many @blocks)
  (model.ball/draw @ball)
  (model.player/draw @player)
  (highlight/highlight-face @player))

(defn add-block [pos]
  (let [block (model.block/create @world pos)]
    (swap! blocks #(conj % block))))

(defn block-finder [pos block]
  (let [[x y z] pos
        phys (:phys block)
        spos (physics/get-position phys)
        [sx sy sz] spos
        width (:width block)
        half (/ width 2.0)]
    (and (>= x (- sx half))
         (< x (+ sx half))
         (>= y (- sy half))
         (< y (+ sy half))
         (>= z (- sz half))
         (< z (+ sz half)))))

(defn find-block [pos]
  (first (filter (fn [block] (block-finder pos block)) @blocks)))

(defn remove-block [pos]
  (when-let [block (find-block pos)]
    (.removeRigidBody @world (:phys block))
    (swap! blocks #(filter (fn [s] (not (= s block))) %))
    @blocks))

(defn init []
  (reset! world  (physics/build-world))
  (reset! ground (model.ground/create @world [0 0 0]))
  (reset! ball   (model.ball/create @world 1.0 [0 5 0]))
  (reset! player (model.player/create @world [-2 0 10]))
  (reset! blocks []))
