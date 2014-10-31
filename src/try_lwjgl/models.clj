(ns try-lwjgl.models
  (:require [try-lwjgl.physics :as physics]
            [try-lwjgl.model.axes :as axes]
            [try-lwjgl.model.grid :as grid]
            [try-lwjgl.model.container-cube :as container-cube]
            [try-lwjgl.model.ground :as model.ground]
            [try-lwjgl.model.stairs :as model.stairs]
            [try-lwjgl.model.ball :as model.ball]
            [try-lwjgl.model.player :as model.player]))

(def world (atom nil))
(def ball (atom nil))
(def ground (atom nil))
(def player (atom nil))
(def stairs (atom nil))

(defn simulate [delta]
  (.stepSimulation @world (* delta 1000.0)))

(defn draw []
  (axes/draw)
  ;(grid/draw)
  (container-cube/draw)
  (model.ground/draw @ground)
  (model.stairs/draw @stairs)
  (model.ball/draw @ball)
  (model.player/draw @player))

(defn add-stair [pos]
  (let [stair (model.stairs/create @world pos)]
    (swap! stairs #(conj % stair))))

(defn stair-finder [pos stair]
  (let [[x y z] pos
        phys (:phys stair)
        spos (physics/get-position phys)
        [sx sy sz] spos
        width (:width stair)
        half (/ width 2.0)]
    (and (>= x (- sx half))
         (< x (+ sx half))
         (>= y (- sy half))
         (< y (+ sy half))
         (>= z (- sz half))
         (< z (+ sz half)))))

(defn find-stair [pos]
  (first (filter (fn [stair] (stair-finder pos stair)) @stairs)))

(defn remove-stair [pos]
  (when-let [stair (find-stair pos)]
    (println "Found stair")
    (.removeRigidBody @world (:phys stair))
    (swap! stairs #(filter (fn [s] (not (= s stair))) %))
    @stairs))

(defn init []
  (reset! world  (physics/build-world))
  (reset! ground (model.ground/create @world [0 0 0]))
  (reset! ball   (model.ball/create @world 1.0 [0 5 0]))
  (reset! player (model.player/create @world [-2 0 10]))
  (reset! stairs []))
