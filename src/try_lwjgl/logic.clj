(ns try-lwjgl.logic
  (:import [org.lwjgl.input Mouse Keyboard]))

(def angle (atom 0.1))

(defn inc-angle []
  (swap! angle (fn [a0] (+ a0 0.5))))

(def int-value (atom 0))

(def state (atom {}))

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
