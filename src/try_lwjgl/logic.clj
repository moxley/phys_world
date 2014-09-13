(ns try-lwjgl.logic
  (:import [org.lwjgl.input Mouse Keyboard]))

(def angle (atom 0.1))

(defn inc-angle []
  (swap! angle (fn [a0] (+ a0 0.5))))

(defn update [delta]
  (let [x (Mouse/getX)
        y (Mouse/getY)
        space (Keyboard/isKeyDown Keyboard/KEY_SPACE)]
    ;;(println "x:", x, ", y:", y)
    ;; Increment angle
    (inc-angle)))
