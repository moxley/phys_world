(ns try-lwjgl.logic
  (:import [org.lwjgl.input Mouse Keyboard]))

(def angle (atom 0.1))
(def int-value (atom 0))
(def state (atom {}))
(def key-map {})
(def key-listeners (atom []))

(defn inc-angle []
  (swap! angle (fn [a0] (+ a0 0.5))))

;;
;; Filtered events
;;

(defn key-down-in-state [key prev-state]
  (prev-state key))

(defn listener-matches-event? [listener event prev-state]
  (and
   (= (event :key) (listener :key))
   (= (event :down?) (listener :down?))
   (and
    (or
     (listener :repeating?)
     (not (key-down-in-state (event :key) prev-state)))
    listener)))

(defn listener-event-matcher [event prev-state]
  (fn [listener]
    (listener-matches-event? listener event prev-state)))

(defn listeners-for-event [event listeners prev-state]
  (filter identity
          (map (listener-event-matcher event prev-state) listeners)))

(defn events-with-listeners [events listeners prev-state]
  (filter (fn [el] (not (empty? (el :listeners))))
          (map (fn [event]
                 {:event event :listeners (listeners-for-event event listeners prev-state)})
               events)))

;;
;; Callback framework
;;

;; Register callback, key, non-repeating?
(defn register-key-callback [callback-spec]
  (swap! key-listeners #(conj % callback-spec)))

(defn callback-keyboard-events [els]
  "Callback matching keyboard events"
  (doseq [event-listeners els]
    (let [event (event-listeners :event)
          listeners (event-listeners :listeners)]
      (doseq [listener listeners]
        ((listener :callback))))))

(defn record-keyboard-events [events state]
  (doseq [event events]
    (swap! state #(assoc % (event :key) true))))

(defn handle-keyboard-events
  ([events] (handle-keyboard-events events @key-listeners state))
  ([events listeners state]
    (let [els (events-with-listeners events listeners @state)
          events (map :event els)]
      (callback-keyboard-events els)
      (record-keyboard-events events state))))

(def key-events (atom []))

;;
;; Extract keyboard events
;;

(defn keyboard-next []
  (new Boolean (Keyboard/next)))

(defn get-event-key []
  (Keyboard/getEventKey))

(defn repeat-event? []
  (new Boolean (Keyboard/isRepeatEvent)))

(defn collect-current-event
  ([key-events key-code repeat-event?]
    (swap! key-events #(conj % {:key (key-map key-code)
                                :repeat? repeat-event?}))))

(defn collect-key-events
  ([] (collect-key-events key-events
                          keyboard-next
                          get-event-key
                          repeat-event?))

  ([key-events next? get-event-key repeat-event?]
    (swap! key-events (fn [x] []))
    (loop [has-event? (next?)]
      (collect-current-event key-events (get-event-key) (repeat-event?))
      (and (next?) (recur true)))))

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
  (def key-map (load-key-codes)))

;;
;; Main
;;

(defn update [delta]
  (let [x (Mouse/getX)
        y (Mouse/getY)
        space (Keyboard/isKeyDown Keyboard/KEY_SPACE)
        left-now-down?  (Keyboard/isKeyDown Keyboard/KEY_LEFT)
        right-now-down? (Keyboard/isKeyDown Keyboard/KEY_RIGHT)]

;;     (case left-now-down?
;;       true
;;       (when (not (@state :left-down))
;;         (println "Left down")
;;         (swap! state #(assoc % :left-down true))
;;         (swap! int-value #(dec %)))

;;       false
;;       (when (@state :left-down)
;;         (println "Left up")
;;         (swap! state #(assoc % :left-down false))))

;;     (case right-now-down?
;;       true
;;       (when (not (@state :right-down))
;;         (println "Right down")
;;         (swap! state #(assoc % :right-down true))
;;         (swap! int-value #(inc %)))

;;       false
;;       (when (@state :right-down)
;;         (println "Right up")
;;         (swap! state #(assoc % :right-down false))))

    (collect-key-events)
    (println "key-events:" @key-events)
    (inc-angle)))

(defn left-key-down []
  (println "left-key-down"))

(defn setup []
  (register-key-callback {:key :left :down? true :repeating? false :callback left-key-down}))
