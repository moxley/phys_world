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

(defn init []
  (swap! world  (fn [_] (physics/build-world)))
  (swap! ground (fn [_] (model.ground/create @world [0 -10 0])))
  (swap! ball   (fn [_] (model.ball/create @world 1.0 [0 5 0])))
  (swap! player (fn [_] (model.player/create @world [-2 0 10])))
  (swap! stairs (fn [_] (model.stairs/create-many @world))))
