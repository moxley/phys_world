(ns try-lwjgl.logic
  (:import [org.lwjgl.input Mouse Keyboard]))

(def angle (atom 0.1))
(def int-value (atom 0))
(def state (atom {}))
(def key-callbacks (atom []))

(defn inc-angle []
  (swap! angle (fn [a0] (+ a0 0.5))))

;; Register callback, key, non-repeating?
(defn register-key-callback [callback-spec]
  (swap! key-callbacks #(conj % callback-spec)))

(defn callback-matches-event? [event]
  (fn [callback]
    (and (= (callback :key) (event :key))
         (= (callback :down?) (event :down?)))))

(defn callback-keyboard-events [events]
  "Callback matching keyboard events"
  (doseq [event events]
    (doseq [cb (filter (callback-matches-event? event) @key-callbacks)]
      (swap! state #(assoc % :left-down true))
      ((cb :callback)))))

(defn record-keyboard-events [events])

(defn handle-keyboard-events [events]
  (callback-keyboard-events events)
  (record-keyboard-events events))

(defn event-matches? [event pattern]
  (and
   (= (event :key) (pattern :key))
   (= (event :down?) (pattern :down?))))

(defn event-matcher [pattern]
  (fn [event] (event-matches? event pattern)))

(defn filtered-events [pattern events]
  (filter (event-matcher pattern) events))

(defn update [delta]
  (let [x (Mouse/getX)
        y (Mouse/getY)
        space (Keyboard/isKeyDown Keyboard/KEY_SPACE)
        left-now-down?  (Keyboard/isKeyDown Keyboard/KEY_LEFT)
        right-now-down? (Keyboard/isKeyDown Keyboard/KEY_RIGHT)]

    (case left-now-down?
      true
      (when (not (@state :left-down))
        (println "Left down")
        (swap! state #(assoc % :left-down true))
        (swap! int-value #(dec %)))

      false
      (when (@state :left-down)
        (println "Left up")
        (swap! state #(assoc % :left-down false))))

    (case right-now-down?
      true
      (when (not (@state :right-down))
        (println "Right down")
        (swap! state #(assoc % :right-down true))
        (swap! int-value #(inc %)))

      false
      (when (@state :right-down)
        (println "Right up")
        (swap! state #(assoc % :right-down false))))
    (inc-angle)))

(defn left-key-down []
  (println "left-key-down"))

(defn setup []
  (register-key-callback {:key :left :down? true :repeating? false :callback left-key-down}))
