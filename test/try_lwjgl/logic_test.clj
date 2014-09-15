(ns try-lwjgl.logic-test
  (:require [clojure.test :refer :all]
            [try-lwjgl.core :refer :all]
            [try-lwjgl.logic :as logic]))

(def test-state (atom {}))

(defn left-key-callback []
  (swap! test-state #(assoc % :left-key-callback-called true)))

(deftest register-key-callback-test
  (testing "It registers a callback for a given key"
    (logic/register-key-callback {:key :left, :callback left-key-callback, :repeating? false})
    (is (= [{:key :left :callback left-key-callback :repeating? false}]
           @logic/key-callbacks))))

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

;; TODO
;;(deftest record-keyboard-events-test
;;  (testing "Keyboard events are recorded to @keyboard-events"
;;    (let [events [{:key :left :down? true}]]
;;      (logic/record-keyboard-events [events])
;;      (is (= @keyboard-events [{:left {:down? true}}])))))

(deftest filtered-events-test
  (let [pattern {:key :left :down? true}
        matching-event {:key :left :down? true :name :matching-event}
        non-matching-event-1 {:key :right :down? true :name :non-matching-event-1}
        non-matching-event-2 {:key :left :down? false :name :non-matching-event-2}
        events [non-matching-event-1 matching-event non-matching-event-2]]
    (testing "Returns empty seq for empty events"
      (is (= (logic/filtered-events pattern []) [])))
    (testing "Returns empty seq for empty pattern"
      (is (= (logic/filtered-events {} events) [])))
    (testing "Returns events matching pattern"
      (is (= (logic/filtered-events pattern events) [matching-event])))))

(filtered-events-test)
