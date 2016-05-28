(ns phys-world.input-test
  (:import [org.lwjgl.input Mouse Keyboard])
  (:require [clojure.test :refer :all]
            [phys-world.core :refer :all]
            [phys-world.input :as input]))

(deftest get-key-map-test
  (testing "It returns a map of integers to keywords"
    (let [key-map (input/get-key-map)]
      (is (= (key-map 30) :a)))))

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
          returned-key-events (input/collect-key-events key-events
                                                        next-fn
                                                        current-key-fn
                                                        key-state?
                                                        is-repeat-event-fn)]
      (is (= 2 (count @key-events)))
      (is (= 2 (count returned-key-events)))
      (is (= {:key :left, :down? true, :repeat? false} (@key-events 0)))
      (is (= {:key :left, :down? true, :repeat? true}  (@key-events 1)))
      (is (= returned-key-events @key-events)))))
