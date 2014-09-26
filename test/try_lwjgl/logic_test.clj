(ns try-lwjgl.logic-test
  (:import [org.lwjgl.input Mouse Keyboard])
  (:require [clojure.test :refer :all]
            [try-lwjgl.core :refer :all]
            [try-lwjgl.logic :as logic]))

(logic/init)

(def test-state (atom {}))

(defn left-key-callback []
  (swap! test-state #(assoc % :left-key-callback-called true)))

(deftest register-key-callback-test
  (testing "It registers a callback for a given key"
    (let [key-listeners (atom [])]
      (logic/register-key-callback key-listeners {:key :left, :down? true, :repeat? false, :callback left-key-callback})
      (is (= [{:key :left, :down? true, :repeat? false, :callback left-key-callback}]
             @key-listeners)))))

(deftest callback-keyboard-events-test
  (testing "Left keydown is recorded to @state"
    (let [test-state (atom {})
          listeners [{:key :left
                      :down? true
                      :callback (fn [] (swap! test-state #(assoc % :left-key-down-called true)))
                      :repeat? false}
                     {:key :left
                      :down? false
                      :callback (fn [] (swap! test-state #(assoc % :left-key-up-called true)))
                      :repeat? false}]
          events [{:key :left, :down? true, :repeat? false}]]
      (logic/callback-keyboard-events events listeners)
      (is (= (@test-state :left-key-down-called) true))
      (is (= (@test-state :left-key-up-called) nil)))))

(deftest listener-matches-event?-test
  (let [listener {:key :left :down? true :repeat? true}
        event {:key :left :down? true :repeat? true}]
    (testing "Returns listener matching event"
      (is (= (logic/listener-matches-event? listener event) listener)))
    (testing "Returns false when repeat? does not match"
      (is (= (logic/listener-matches-event? (assoc listener :repeat? false) event) false)))))

(deftest listeners-for-event-test
  (let [event {:key :left :down? true :repeat? false}
        listener {:key :left :down? true :repeat? false}
        listeners [listener]]
    (testing "Returns listener matching event"
      (is (= (logic/listeners-for-event event listeners) [listener])))))

(deftest events-with-listeners-test
  (let [event {:key :left :down? true :repeat? false}
        events [{:key :right :down? true :repeat? true}
                event
                {:key :1 :down? true :repeat? false}]
        listener {:key :left :down? true :repeat? false}
        listeners [listener
                   {:key :right :down? true :repeat? false}]]
    (testing "Returns matching events, combined with their listeners"
      (is (= (logic/events-with-listeners events listeners)
             [[event (list listener)]])))))

(deftest handle-keyboard-events-test
  (let [events [{:key :left,  :down? true, :repeat? true}
                {:key :right, :down? true, :repeat? false}
                {:key :1,     :down? true, :repeat? false}]
        test-state (atom {})
        left-callback (fn [] (swap! test-state #(assoc % :left-callback-called true)))
        right-callback (fn [] (swap! test-state #(assoc % :right-callback-called true)))
        listeners [{:key :left, :down? true :repeat? false :callback left-callback}
                   {:key :right, :down? true, :repeat? false :callback right-callback}]]
    (testing "It calls the appropriate callbacks and records the events"
      (logic/handle-keyboard-events events listeners)
      (is (= {:right-callback-called true} @test-state)))))

(deftest collect-key-events-test
  (testing "It collects events into key-events"
    (let [key-events    (atom []) ;; Value used in dependency injection
          source-events [{:key Keyboard/KEY_LEFT, :down? true, :repeat? false}
                         {:key Keyboard/KEY_LEFT, :down? true, :repeat? true}]
          current-index (atom 0)
          current-event (atom nil)
          next-fn (fn []
                    (when (< @current-index (count source-events))
                      (swap! current-event (fn [_] (source-events @current-index)))
                      (swap! current-index #(inc %))
                      true))
          current-key-fn     (fn [] (@current-event :key))
          is-repeat-event-fn (fn [] (@current-event :repeat?))
          key-state? (fn [] (@current-event :down?))
          returned-key-events (logic/collect-key-events key-events
                                                        next-fn
                                                        current-key-fn
                                                        key-state?
                                                        is-repeat-event-fn)]
      (is (= 2 (count @key-events)))
      (is (= 2 (count returned-key-events)))
      (is (= {:key :left, :down? true, :repeat? false} (@key-events 0)))
      (is (= {:key :left, :down? true, :repeat? true}  (@key-events 1)))
      (is (= returned-key-events @key-events)))))

;; TODO handle key-up
