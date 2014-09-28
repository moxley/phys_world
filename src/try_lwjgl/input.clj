(ns try-lwjgl.input
  (:import [org.lwjgl.input Mouse Keyboard]))

(def key-map {})
(def key-listeners (atom []))
(def key-events (atom []))

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
  ([key-events key-code down? repeat-event?]
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
;; Event filtering
;;

(defn listener-matches-event? [listener event]
  (and
   (= (event :key) (listener :key))
   (= (event :down?) (listener :down?))
   (and
    (or
     (listener :repeat?)
     (not (event :repeat?))))
    listener))

(defn listeners-for-event [event listeners]
  (filter identity
          (map (fn [listener] (listener-matches-event? listener event))
               listeners)))

(defn events-with-listeners [events listeners]
  (filter (fn [el] (not (empty? (last el))))
          (map (fn [event] [event (listeners-for-event event listeners)])
               events)))

;;
;; Callback framework
;;

;; Register listener, key, non-repeat?
(defn register-key-callback
  ([listener] (register-key-callback key-listeners listener))
  ([key-listeners listener]
   (swap! key-listeners #(conj % listener))))

(defn callback-listeners-for-event
  "Callback matching keyboard events"
  ([event matching-listeners]
   (doseq [listener matching-listeners]
     ((listener :callback)))))

(defn callback-keyboard-events
  ([events] (callback-keyboard-events events @key-listeners))
  ([events listeners]
   (doseq [event events]
     (callback-listeners-for-event
      event
      (listeners-for-event event listeners)))))

(defn handle-keyboard-events
  ([] (handle-keyboard-events (collect-key-events) @key-listeners))
  ([events listeners]
   (callback-keyboard-events events listeners)))

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

(defn append-key-code-line [key-map line]
  (let [[code-str name] (clojure.string/split line (re-pattern " "))
         code (Integer/parseInt code-str)
         key (keyword name)]
    (assoc key-map code key)))

(defn load-key-codes []
  (with-open [rdr (clojure.java.io/reader "key_codes.txt")]
    (reduce append-key-code-line {} (line-seq rdr))))

(defn init []
  (def key-map (load-key-codes))
  (set-mouse-grabbed true))
