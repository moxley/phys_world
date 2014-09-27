(ns try-lwjgl.logic
  (:import [org.lwjgl.input Mouse Keyboard])
  (:require [try-lwjgl.input :as input]))

(def angle (atom 0.1))

(defn inc-angle []
  (swap! angle (fn [a0] (+ a0 0.5))))

(defn key-left-down []
  (println "Key LEFT down"))

(defn register-listeners []
  (input/register-key-callback {:key :left, :down? true, :repeat? false :callback key-left-down}))

(defn init []
  (input/init)
  (register-listeners))

;;
;; Main
;;

(defn update [delta]
  (let [x (Mouse/getX)
        y (Mouse/getY)]

    (input/handle-keyboard-events)
    (inc-angle)))
