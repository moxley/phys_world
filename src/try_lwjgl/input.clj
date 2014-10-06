(ns try-lwjgl.input
  (:import [org.lwjgl.input Mouse Keyboard]))

(def key-map (atom nil))
(def key-listeners (atom []))
(def key-events (atom []))

;;
;; Key map
;;
(defn append-key-code-line [key-map line]
  (let [[code-str name] (clojure.string/split line (re-pattern " "))
        code (Integer/parseInt code-str)
        key (keyword name)]
    (assoc key-map code key)))

(defn load-key-codes []
  (with-open [rdr (clojure.java.io/reader "key_codes.txt")]
    (reduce append-key-code-line {} (line-seq rdr))))

(defn get-key-map []
  (or @key-map
      (swap! key-map (fn [_] (load-key-codes)))))

;;
;; Abstraction over LWJGL's Keyboard API
;;

(defn keyboard-next? []
  (Keyboard/next))

(defn get-event-key []
  (Keyboard/getEventKey))

(defn repeat-event? []
  (Keyboard/isRepeatEvent))

(defn key-state? []
  (Keyboard/getEventKeyState))

(defn collect-current-event
  [key-events key-code down? repeat-event?]
  (let [key-map (get-key-map)]
    (swap! key-events #(conj % {:key (key-map key-code)
                                :down? down?
                                :repeat? repeat-event?}))))

(defn collect-key-events
  ([] (collect-key-events key-events
                          keyboard-next?
                          get-event-key
                          key-state?
                          repeat-event?))

  ([key-events keyboard-next? get-event-key key-state? repeat-event?]
   (swap! key-events (fn [x] []))
   (loop [has-event? (keyboard-next?)]
     (when has-event?
       (collect-current-event key-events (get-event-key) (key-state?) (repeat-event?))
       (recur (keyboard-next?))))
   @key-events))

;;
;; Mouse
;;

(defn set-mouse-grabbed [grabbed?]
  (Mouse/setGrabbed grabbed?))

(defn mouse-dx []
  (Mouse/getDX))

(defn mouse-dy []
  (Mouse/getDY))

;;
;; Initialize
;;

(defn init []
  (set-mouse-grabbed false))
