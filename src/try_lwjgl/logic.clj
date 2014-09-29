(ns try-lwjgl.logic
  (:require [try-lwjgl.input :as input]))

(def angle (atom 0.1))
(def angle-speed (atom 0.5))
(def mode (atom :angle-speed))
(def player-position (atom [(float 0.0) (float 1.0) (float 10.0)]))
(def player-direction (atom [(float 0.0) (float 0.0) (float 0.0)]))
(def show-player? (atom false))

(defn inc-angle []
  (swap! angle #(+ % @angle-speed)))

(defn rotate-direction [direction axis angle]
  (assoc direction axis (+ (direction axis) angle)))

(defn translate-with-direction
  ([position direction] (translate-with-direction position direction (float 1.0)))
  ([position direction distance]
     ;; Direction, measured in 3 angles.
     ;; (0, 0, 0) is origin, upright, facing -z
     (let [[angle-x angle-y angle-z] direction
           [px py pz] position
           dx (* -1.0 distance (Math/cos angle-x) (Math/sin angle-y))
           dy (* -1.0 distance (Math/sin angle-x))
           dz (* -1.0 distance (Math/cos angle-y) (Math/cos angle-x))
           new-x (+ px dx)
           new-y (+ py dy)
           new-z (+ pz dz)]
       [new-x new-y new-z])))

(defn print-player []
  (when @show-player?
    (println "player-position:" @player-position
             ", player-direction:" @player-direction
             ", direction point:" (translate-with-direction @player-position @player-direction))))

(defn player-moved []
  (print-player))

(defn key-left-down []
  (swap! player-direction #(rotate-direction % 1 0.1))
  (player-moved)
  @player-direction)

(defn key-right-down []
  (swap! player-direction #(rotate-direction % 1 -0.1))
  (player-moved)
  @player-direction)

(def up-down-callbacks
  {:angle-speed {:atom angle-speed, :amount 0.1}})

(defn adjust-value [type up?]
  (let [spec (type up-down-callbacks)
        value-atom (:atom spec)
        amount (:amount spec)]
    (swap! value-atom #(identity (if up? (+ % amount) (- % amount))))))

(defn key-up []
  (adjust-value @mode true))

(defn key-down []
  (adjust-value @mode false))

(defn set-mode [m]
  (println "Mode:" m)
  (swap! mode (fn [_] identity m)))

(defn move [direction amount]
  (swap! player-position (fn [pp] (translate-with-direction pp direction amount)))
  (player-moved)
  @player-position)

(defn move-sideways [amount]
  (move (rotate-direction @player-direction 1 (* -0.5 Math/PI)) amount))

(defn move-forward [amount]
  (move @player-direction amount))

(def mouse-sensitivity 0.002)

(defn handle-mouse []
  (let [dx (input/mouse-dx)
        dy (input/mouse-dy)]
    (when (not (= dx 0))
      (swap! player-direction #(rotate-direction % 1 (* -1.0 dx mouse-sensitivity))))
    (when (not (= dy 0))
      (swap! player-direction #(rotate-direction % 0 (* -1.0 dy mouse-sensitivity))))))

(defn handle-movement-key-up [key event key-down-at]
  (if (and event (not (event :down?)))
    ;; Key up
    (when @key-down-at
      (swap! key-down-at (fn [_] nil)))))

(defn handle-movement-key-down [key event key-down-at move-fn]
  (when (or (and event (event :down?)) @key-down-at)
    (move-fn key)
    (if-not @key-down-at (swap! key-down-at (fn [_] (System/currentTimeMillis))))))

(def keys-down-at
  {:a (atom nil)
   :d (atom nil)
   :w (atom nil)
   :s (atom nil)})

(defn event-move [key event move-fn]
  (let [key-down-at (keys-down-at key)]
    (handle-movement-key-up key event key-down-at)
    (handle-movement-key-down key event key-down-at move-fn)))

(defn strife [key event]
  (event-move key event (fn [key] (move-sideways (key {:a -0.1 :d 0.1})))))

(defn event-move-forward [key event]
  (event-move key event (fn [key] (move-forward (key {:w 0.1 :s -0.1})))))

(def key-listeners
  {:w event-move-forward
   :s event-move-forward
   :a strife
   :d strife
   :1 (fn [key event] (when (and event (event :down?) (not (event :repeat?)))
                        (swap! show-player? #(not %))))})

(defn events-by-key [events]
  (reduce (fn [m event] (assoc m (event :key) event))
          {} events))

(defn handle-keyboard []
  (let [events (input/collect-key-events)
        events-by-key (events-by-key events)]

    (doseq [[key listener] key-listeners]
      (let [event (events-by-key key)]
        (listener key event)))))

(defn init []
  (input/init)
  (print-player))

(defn update [delta]
  (handle-keyboard)
  (handle-mouse)
  (inc-angle))
