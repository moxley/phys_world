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
    (logic/register-key-callback {:key :left, :callback left-key-callback, :repeating? false})
    (is (= [{:key :left :callback left-key-callback :repeating? false}]
           @logic/key-listeners))))

(deftest key-down-test
  (testing "Left keydown is recorded to @state"
    (logic/register-key-callback {:key :left
                                  :down? true
                                  :callback left-key-callback
                                  :repeating? false})
    (logic/register-key-callback {:key :left
                                  :down? false
                                  :callback (fn [] (swap! test-state #(assoc % :left-key-up-called true)))
                                  :repeating? false})
    (let [events [{:key :left :down? true}]]
      (logic/callback-keyboard-events events)
      (is (@test-state :left-key-callback-called) true)
      (is (= (@test-state :left-key-up-called) nil)))))

(deftest listener-matches-event?-test
  (let [listener {:key :left :down? true}
        event {:key :left :down? true}
        prev-state {:right true}]
    (testing "Returns listener matching event when not found in prev-state"
      (is (= (logic/listener-matches-event? listener event prev-state) listener)))
    (testing "Returns false when matches event, but found in prev-state"
      (is (= (logic/listener-matches-event? listener event {:left true}) false)))))

(deftest listeners-for-event-test
  (let [event {:key :left :down? true}
        listener {:key :left :down? true :repeating? false}
        listeners [listener]
        prev-state {:right true}]
    (testing "Returns listener matching event"
      (is (= (logic/listeners-for-event event listeners prev-state) [listener])))
    (testing "Does not return matching listener if key found in prev-state"
      (is (= (logic/listeners-for-event event listeners {:left true}) [])))))

(deftest events-with-listeners-test
  (let [event {:key :left :down? true}
        events [{:key :right :down? true}
                event
                {:key :1 :down? true}]
        listener {:key :left :down? true}
        listeners [listener
                   {:key :right :down? true}]
        prev-state {:right true}]
    (testing "Returns matching events, combined with their listeners"
      (is (= (logic/events-with-listeners events listeners prev-state) [{:event event :listeners [listener]}])))))

(deftest handle-keyboard-events-test
  (let [events [{:key :left, :down? true}
                {:key :right, :down? true}
                {:key :1, :down? true}]
        test-state (atom {})
        left-callback (fn [] (swap! test-state #(assoc % :left-callback-called true)))
        right-callback (fn [] (swap! test-state #(assoc % :right-callback-called true)))
        listeners [{:key :left, :down? true :repeating? false :callback left-callback}
                   {:key :right, :down? true, :repeating? false :callback right-callback}]
        state (atom {:left true})]
    (testing "It calls the appropriate callbacks and records the events"
      (logic/handle-keyboard-events events listeners state)
      (is (= {:right-callback-called true} @test-state))
      (is (= {:left true :right true} @state)))))

(deftest collect-key-events-test
  (testing "It collects events into key-events"
    (let [key-events    (atom []) ;; Value used in dependency injection
          source-events [{:key Keyboard/KEY_LEFT, :repeat? false}
                         {:key Keyboard/KEY_LEFT, :repeat? true}]
          current-index (atom 0)
          current-event (atom nil)
          next-fn (fn []
                    (when (< @current-index (count source-events))
                      (swap! current-event (fn [_] (source-events @current-index)))
                      (swap! current-index #(inc %))
                      true))
          current-key-fn     (fn [] (@current-event :key))
          is-repeat-event-fn (fn [] (@current-event :repeat?))]
      (logic/collect-key-events key-events
                                next-fn
                                current-key-fn
                                is-repeat-event-fn)
      (is (= 2 (count @key-events)))
      (is (= {:key :key_left, :repeat? false} (@key-events 0)))
      (is (= {:key :key_left, :repeat? true}  (@key-events 1))))))

(collect-key-events-test)

;; TODO handle key-up
