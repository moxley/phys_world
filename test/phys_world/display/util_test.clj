(ns phys-world.display.util-test
  (:require [clojure.test :refer :all]
            [phys-world.display.util :as util]))

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
