(ns try-lwjgl.logic-test
  (:require [clojure.test :refer :all]
            [try-lwjgl.core :refer :all]
            [try-lwjgl.logic :as logic]))

(deftest closest-pointed-face-test
  (let [block-positions [[0.5 0.5 -0.5] [0.5 0.5 0.5]]]
    ;; Front-facing (+z)
    (let [arm [[0.5 0.5 1.5] [0.5 0.5 -1.5]]
          intersection (logic/closest-pointed-face block-positions arm)]
      (is (= intersection {:intersect [0.5 0.5 1.0]
                           :face [[0.0 0.0 1.0] [1.0 1.0 1.0]]
                           :block-position [0.5 0.5 0.5]})))

    ;; Side-facing (+x), pointing at back block (-z)
    (let [arm [[1.5 0.5 -0.5] [-0.5 0.5 -0.5]]
          intersection (logic/closest-pointed-face block-positions arm)]
      (is (= intersection {:intersect [1.0 0.5 -0.5]
                           :face [[1.0 0.0 0.0] [1.0 1.0 1.0]]
                           :block-position [0.5 0.5 -0.5]})))))
