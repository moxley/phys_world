(ns phys-world.loop
  (:import [org.lwjgl.opengl Display]
           [org.lwjgl Sys])
  (:require [phys-world.logic :as logic]
            [phys-world.display :as display]
            [phys-world.frame :as frame]))

(def last-frame-time (atom (Sys/getTime)))

(defn get-delta []
  (let [time (/ (* (Sys/getTime) 1000) (Sys/getTimerResolution))
        delta (int (- time @last-frame-time))]
    (swap! last-frame-time (fn [lft t] t) time)
    delta))

(defn run []
  (while (not (Display/isCloseRequested))
    (let [delta (get-delta)]
      (frame/iteration delta)
      (Display/update)
      (Display/sync 60)))
  (Display/destroy))
