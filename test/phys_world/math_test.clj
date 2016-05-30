(ns phys-world.math-test
  (:require [clojure.test :refer :all]
            [phys-world.math :as math]))

;; line: [[-2.0 0.9958073 10.0] (4.440892098500626E-16 0.0 2.0)] , plane: [[0.09973877668380737 -0.05938476324081421 0.09973877668380737] [0.0 1.0 0.0]]
(deftest line-plane-intersect-test
  (let [line [[-2.0 0.9958073 10.0] (list 4.440892098500626E-16 0.0 2.0)]
        plane [[0.09973877668380737 -0.05938476324081421 0.09973877668380737] [0.0 1.0 0.0]]
        answer (math/line-plane-intersect line plane)]
    (is (= answer nil)))

  (let [line [[0 1.0 0] [0.0 -2.0 0.0]]
        plane [[0.0 0.0 0.0] [0.0 1.0 0.0]]
        answer (math/line-plane-intersect line plane)]
    (is (= answer [0.0 0.0 0.0])))

  (let [line [[0 -1.0 0] [0.0 2.0 0.0]]
        plane [[0.0 0.0 0.0] [0.0 1.0 0.0]]
        answer (math/line-plane-intersect line plane)]
    (is (= answer [0.0 0.0 0.0]))))
