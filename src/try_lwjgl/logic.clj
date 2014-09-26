(ns try-lwjgl.logic
  (:import [org.lwjgl.input Mouse Keyboard]))

(def angle (atom 0.1))
(def int-value (atom 0))
(def state (atom {}))
(def key-map {})
(def key-listeners (atom []))
(def key-events (atom []))

(defn inc-angle []
  (swap! angle (fn [a0] (+ a0 0.5))))

;;
;; Extract keyboard events from LWJGL
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

(defn key-down-in-state [key prev-state]
  (prev-state key))

(defn listener-matches-event? [listener event]
  (and
   (= (event :key) (listener :key))
   (= (event :down?) (listener :down?))
   (and
    (or
     (listener :repeat?)
     (not (event :repeat?))))
    listener))

;; (defn listener-event-matcher [event]
;;   (fn [listener]
;;     (listener-matches-event? listener event)))

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

(defn key-left-down []
  (println "Key LEFT down"))

(defn register-listeners []
  (swap! key-listeners #(conj % {:key :left, :down? true, :repeat? false :callback key-left-down})))

(defn init []
  (def key-map (load-key-codes))
  (register-listeners))

;;
;; Main
;;

(defn update [delta]
  (let [x (Mouse/getX)
        y (Mouse/getY)]

    (handle-keyboard-events)
    (inc-angle)))

(defn left-key-down []
  (println "left-key-down"))

(defn setup []
  (register-key-callback {:key :left :down? true :repeat? false :callback left-key-down}))
