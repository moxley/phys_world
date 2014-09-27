(ns try-lwjgl.logic
  (:import [org.lwjgl.input Mouse Keyboard])
  (:require [try-lwjgl.input :as input]))

(def angle (atom 0.1))
(def angle-speed (atom 0.5))
(def mode (atom :angle-speed))

(defn inc-angle []
  (swap! angle #(+ % @angle-speed)))

(defn key-left-down []
  (println "Key LEFT down"))

(def up-down-callbacks
  {:angle-speed {:atom angle-speed, :amount 0.1}})

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

(def player-position (atom [0.0, 1.0, 10.0]))

(defn move [axis amount]
  (swap! player-position (fn [pos]
                           (let [old-val (pos axis)
                                 new-val (+ old-val (* amount 1.0))]
                             (assoc pos axis new-val)))))

(defn move-sideways [amount]
  (move 0 amount))

(defn move-forward [amount]
  (move 2 (* -1.0 amount)))

(def listeners
  [{:key :left, :down? true, :repeat? false :callback key-left-down}

   ;; Navigation
   {:key :a, :down? true, :repeat? false :callback (fn [] (move-sideways -1.0))}
   {:key :d, :down? true, :repeat? false :callback (fn [] (move-sideways 1.0))}
   {:key :w, :down? true, :repeat? false :callback (fn [] (move-forward 1.0))}
   {:key :s, :down? true, :repeat? false :callback (fn [] (move-forward -1.0))}

   ;; Debugging keys
   {:key :up, :down? true, :repeat? false :callback key-up}
   {:key :down, :down? true, :repeat? false :callback key-down}
   {:key :1, :down? true, :repeat? false :callback (fn [] (set-mode :angle-speed))}
   {:key :2, :down? true, :repeat? false :callback (fn [] (set-mode :eye-z))}])

(defn register-listeners []
  (doseq [listener listeners]
    (input/register-key-callback listener)))

(defn init []
  (input/init)
  (register-listeners))

(defn update [delta]
  (let [x (Mouse/getX)
        y (Mouse/getY)]

    (input/handle-keyboard-events)
    (inc-angle)))
