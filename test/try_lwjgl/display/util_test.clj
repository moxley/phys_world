(ns try-lwjgl.display.util-test
  (:require [clojure.test :refer :all]
            [try-lwjgl.display.util :as util]))

(deftest next-vertex-test
  (testing "It is a sequence of valid vertices"
    (is (= (util/next-vertex [0] [1]) [1]))
    (is (= (util/next-vertex [1] [0]) [0]))
    (is (= (util/next-vertex [0] [0]) [0]))
    (is (= (util/next-vertex [1] [1]) [1]))
    
    (is (= (util/next-vertex [0 0] [1 1]) [1 0]))
    (is (= (util/next-vertex [1 1] [0 0]) [0 1]))
    (is (= (util/next-vertex [0 1] [0 0]) [0 0]))

    (is (= (util/next-vertex [0 0 0] [1 1 0]) [1 0 0]))
    (is (= (util/next-vertex [1 1 0] [0 0 0]) [0 1 0]))
    (is (= (util/next-vertex [0 1 0] [0 0 0]) [0 0 0]))))
