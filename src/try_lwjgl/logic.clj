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
           dx (* -1.0 distance (Math/sin angle-y))
           dy 0.0
           dz (* -1.0 distance (Math/cos angle-y))
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

(def listeners
  [{:key :left, :down? true, :repeat? false :callback key-left-down}
   {:key :right, :down? true, :repeat? false :callback key-right-down}

   ;; Navigation
   {:key :a, :down? true, :repeat? false :callback (fn [] (move-sideways -1.0))}
   {:key :d, :down? true, :repeat? false :callback (fn [] (move-sideways 1.0))}
   {:key :w, :down? true, :repeat? false :callback (fn [] (move-forward 1.0))}
   {:key :s, :down? true, :repeat? false :callback (fn [] (move-forward -1.0))}

   ;; Debugging keys
   {:key :up, :down? true, :repeat? false :callback key-up}
   {:key :down, :down? true, :repeat? false :callback key-down}
   {:key :1, :down? true, :repeat? false :callback (fn [] (swap! show-player? #(not %)))}
   {:key :2, :down? true, :repeat? false :callback (fn [] (set-mode :eye-z))}])

(defn register-listeners []
  (doseq [listener listeners]
    (input/register-key-callback listener)))

(defn init []
  (input/init)
  (register-listeners)
  (print-player))

(def mouse-sensitivity 0.002)

(defn handle-mouse []
  (let [dx (input/mouse-dx)]
    (when (not (= dx 0))
      (swap! player-direction #(rotate-direction % 1 (* -1.0 dx mouse-sensitivity))))))

(defn update [delta]
  (input/handle-keyboard-events)
  (handle-mouse)
  (inc-angle))
