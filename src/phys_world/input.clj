(ns phys-world.input
  (:import [org.lwjgl.input Mouse Keyboard]))

(def key-map (atom nil))
(def key-events (atom nil))
(def mouse-events (atom nil))
(def mouse-grabbed? (atom false))

;;
;; Key map
;;
(defn append-key-code-line [key-map line]
  (let [[code-str name] (clojure.string/split line (re-pattern " "))
        code (Integer/parseInt code-str)
        key (keyword name)]
    (conj key-map {code key, key code})))

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

(defn key-down? [key]
  (let [key-map (get-key-map)
        code (key-map key)]
    (Keyboard/isKeyDown code)))

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
     (reset! key-events [])
     (loop [has-event? (keyboard-next?)]
       (when has-event?
         (collect-current-event key-events (get-event-key) (key-state?) (repeat-event?))
         (recur (keyboard-next?))))
     @key-events))

(defn get-key-events []
  (let [events (or @key-events (collect-key-events))]
    events))

(defn key-down-event [key]
  (first (filter #(and (= key (:key %))
                       (:down? %))
                 (get-key-events))))

;;
;; Mouse
;;

(defn mouse-next? [] (Mouse/next))
(defn mouse-event-button [] (Mouse/getEventButton))
(defn mouse-event-button-state [] (Mouse/getEventButtonState))

(defn collect-mouse-events []
  (loop [has-event? (mouse-next?)
         events []]
    (if-not has-event?
      events
      (let [btn (mouse-event-button)
            down? (mouse-event-button-state)]
        (recur (mouse-next?)
               (conj events {:button btn :down? down?}))))))

(defn get-mouse-events []
  (or @mouse-events (reset! mouse-events (collect-mouse-events))))

(defn set-mouse-grabbed [grabbed?]
  (when (or (and (not grabbed?) @mouse-grabbed?)
            (and grabbed? (not @mouse-grabbed?)))
    (Mouse/setGrabbed grabbed?)
    (swap! mouse-grabbed? (fn [_] grabbed?))))

(defn mouse-dx []
  (Mouse/getDX))

(defn mouse-dy []
  (Mouse/getDY))

(defn mouse-left-click? [])

(defn mouse-left-down? []
  (and (Mouse/isButtonDown 0)
       (not (key-down? :lcontrol))))

(defn mouse-right-down? []
  (or (Mouse/isButtonDown 2)
      (and (Mouse/isButtonDown 0)
           (key-down? :lcontrol))))

(defn iteration [delta]
  (reset! key-events nil)
  (reset! mouse-events nil))

;;
;; Initialize
;;

(defn init []
  (set-mouse-grabbed true))
