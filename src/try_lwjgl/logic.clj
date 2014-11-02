(ns try-lwjgl.logic
  (:require [try-lwjgl.input :as input]
            [try-lwjgl.model.player :as model.player]
            [try-lwjgl.models :as models]
            [try-lwjgl.math :as math]
            [try-lwjgl.physics :as physics]
            [try-lwjgl.model.highlight :as highlight]))

(def angle (atom 0.1))
(def angle-speed (atom 0.5))
(def mode (atom :angle-speed))
(def player-position (atom [(float 0.0) (float 1.0) (float 10.0)]))
(def player-direction (atom [(float 0.0) (float 0.0) (float 0.0)]))
(def show-player? (atom false))
(def keys-down-at {})

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

(defn strife [amount]
  (move (rotate-direction @player-direction 1 (* -0.5 Math/PI)) amount))

(defn move-forward [amount]
  (move @player-direction amount))

(defn move-vertical [amount]
  (swap! player-position (fn [pp] (assoc pp 1 (+ (pp 1) amount)))))

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

(defn event-move [key event move-fn]
  (let [key-down-at (keys-down-at key)]
    (handle-movement-key-up key event key-down-at)
    (handle-movement-key-down key event key-down-at move-fn)))

(defn event-strife [key event]
  (event-move key event (fn [key] (strife (key {:a -0.1 :d 0.1})))))

(defn event-move-forward [key event]
  (event-move key event (fn [key] (move-forward (key {:w 0.1 :s -0.1})))))

(defn event-move-vertical [key event]
  (event-move key event (fn [key] (move-vertical (key {:space 0.1 :lshift -0.1})))))

(defn events-by-key [events]
  (reduce (fn [m event] (assoc m (event :key) event))
          {} events))

(def key-listeners
  {:w event-move-forward
   :s event-move-forward
   :a event-strife
   :d event-strife
   :1 (fn [key event] (when (and event (event :down?) (not (event :repeat?)))
                        (swap! show-player? #(not %))))
   :space event-move-vertical
   :lshift event-move-vertical})

(def keys-down-at
  "Maps keyboard key to timestamp atom: {:keyboard-key (atom nil)}"
  (reduce (fn [m key] (assoc m key (atom nil)))
          {}
          (keys key-listeners)))

(defn handle-keyboard []
  (let [events (input/get-key-events)
        events-by-key (events-by-key events)]

    (doseq [[key listener] key-listeners]
      (let [event (events-by-key key)]
        (listener key event)))))

(defn actions [player]
  (let [events (input/get-mouse-events)
        mouse-left? (first (filter #(and (= (:button %) 0) (:down? %) (not (input/key-down? :lcontrol))) events))
        mouse-right? (first (filter #(and (= (:button %) 0) (:down? %) (input/key-down? :lcontrol)) events))]
    (when mouse-left?
      (let [ppos (physics/get-position (:phys player))
            pos (model.player/forward-position player)]
        (models/remove-block pos)))
    (when mouse-right?
      (let [ppos (physics/get-position (:phys player))
            pos (model.player/forward-position player)]
        (models/add-block pos)))
    (when (input/key-down-event :1)
      (models/add-block [0.5 0.5 0.5]))))

(defn highlight-pointed-face
  ([player]
     (let [player-pos (physics/get-position (:phys player))
           pointer-pos (model.player/forward-position player)]
       (highlight-pointed-face player-pos pointer-pos)))
  ([player-pos pointer-pos]
     (let [p1 player-pos
           p2 pointer-pos])))

(defn player-logic [delta player]
  (model.player/movement delta player)
  (model.player/orientation delta player)
  ;;(highlight-pointed-face player)
  (actions player))

(defn handle-input [delta]
  (input/iteration delta)
  ;; TODO Use input/key-down-event instead
  (doseq [event (input/get-key-events)]
    (let [[key down? repeat?] (map #(event %) [:key :down? :repeat?])]
      (cond
       (= :g key) (input/set-mouse-grabbed true)
       (= :r key) (input/set-mouse-grabbed false)))))

(defn frame [delta]
  (handle-input delta)
  (player-logic delta @models/player))

(defn init []
  (input/init)
  (print-player))
