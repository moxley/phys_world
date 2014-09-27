(ns try-lwjgl.logic
  (:import [org.lwjgl.input Mouse Keyboard])
  (:require [try-lwjgl.input :as input]))

(def angle (atom 0.1))
(def angle-speed (atom 0.5))
(def eye-z (atom 10.0))
(def mode (atom :angle-speed))

(defn inc-angle []
  (swap! angle #(+ % @angle-speed)))

(defn key-left-down []
  (println "Key LEFT down"))

(def up-down-callbacks
  {:angle-speed {:atom angle-speed, :amount 0.1}
   :eye-z {:atom eye-z, :amount 1.0}})

(defn adjust-value [type up?]
  (let [spec (type up-down-callbacks)
        value-atom (:atom spec)
        amount (:amount spec)]
    (swap! value-atom #(identity (if up? (+ % amount) (- % amount))))
    (println "New value:" @value-atom)
    @value-atom))

(defn key-up []
  (adjust-value @mode true))

(defn key-down []
  (adjust-value @mode false))

(defn set-mode [m]
  (println "Mode:" m)
  (swap! mode (fn [_] identity m)))

(defn register-listeners []
  (input/register-key-callback {:key :left, :down? true, :repeat? false :callback key-left-down})
  (input/register-key-callback {:key :up, :down? true, :repeat? false :callback key-up})
  (input/register-key-callback {:key :down, :down? true, :repeat? false :callback key-down})
  (input/register-key-callback {:key :1, :down? true, :repeat? false :callback (fn [] (set-mode :angle-speed))})
  (input/register-key-callback {:key :2, :down? true, :repeat? false :callback (fn [] (set-mode :eye-z))}))

(defn init []
  (input/init)
  (register-listeners))

(defn update [delta]
  (let [x (Mouse/getX)
        y (Mouse/getY)]

    (input/handle-keyboard-events)
    (inc-angle)))
