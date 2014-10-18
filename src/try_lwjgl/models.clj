(ns try-lwjgl.models
  (:require [try-lwjgl.physics :as physics]
            [try-lwjgl.model.ground :as model.ground]
            [try-lwjgl.model.ball :as model.ball]
            [try-lwjgl.model.player :as model.player]))

(def world (atom nil))
(def ball (atom nil))
(def ground (atom nil))
(def player (atom nil))

(defn simulate [delta]
  (.stepSimulation @world (* delta 1000.0)))

(defn draw []
  (model.ground/draw @ground)
  (model.ball/draw @ball)
  (model.player/draw @player))

(defn init []
  (swap! world (fn [_] (physics/build-world)))
  (swap! ground (fn [_] (model.ground/create @world [0 -10 0])))
  (swap! ball (fn [_] (model.ball/create @world 1.0 [0 5 0])))
  (swap! player (fn [_] (model.player/create @world [-2 0 10]))))
