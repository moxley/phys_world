(ns try-lwjgl.loop
  ;;(:gen-class)
  (:import [org.lwjgl.opengl Display]
           [org.lwjgl Sys])
  (:require [try-lwjgl.logic :as logic]
            [try-lwjgl.display :as display]))

(def last-frame-time (atom (Sys/getTime)))

(defn get-delta []
  (let [time (/ (* (Sys/getTime) 1000) (Sys/getTimerResolution))
        delta (int (- time @last-frame-time))]
    (swap! last-frame-time (fn [lft t] t) time)
    delta))

(defn run []
  (while (not (Display/isCloseRequested))
    (let [delta (get-delta)]
      ;;(logic/update delta)
      (display/iteration delta)
      (Display/update)
      (Display/sync 60)))
  (Display/destroy))
