(ns try-lwjgl.model.block-test
  (:require [clojure.test :refer :all]
            [try-lwjgl.model.block :as model.block]))

;; Calculating arm-face intersect
;;
;; Need: 1. Absolute intersection
;;       2. Relative face
;;
;; For the face, relative to the block's position:
;;   1. Translate (add) it by the block's position.
;;   2. Calculate the intersect.
;;   3. This is the absolute intersect

;; Calculate the absolute arm-face intersect
(deftest arm-face-intersect-test
  ;; Lower form is the absolute calculation
  (let [arm [[0.5 0.5 0.5] [0.5 0.5 -0.5]]
        face [[0.0 0.0 0.0] [1.0 1.0 0.0]]
        intersect (model.block/arm-face-intersect arm face)]
    (is (= intersect [0.5 0.5 0.0])))
  (let [arm [[0.5 1.5 0.5] [0.5 -0.5 0.5]]]
    (is (= [0.5 0.0 0.5]
           (model.block/arm-face-intersect arm [[0.0 0.0 0.0] [1.0 0.0 1.0]])))))

(deftest arm-block-intersects-test
  (let [block-pos [0.5 0.5 0.5]
        arm [[0.5 -0.5 0.5] [0.5 1.5 0.5]]
        intersects (model.block/arm-block-intersects arm block-pos model.block/FACES)]
    (is (= [nil [0.5 0.0 0.5] nil nil [0.5 1.0 0.5] nil] intersects))))

(deftest closest-intersection-test
  (let [block-pos [0.5 0.5 0.5]]
    ;; From the front +z
    (let [arm [[0.5 0.5 1.5] [0.5 0.5 -0.5]]]
      (is (= {:face [[0.0 0.0 1.0] [1.0 1.0 1.0]]
              :intersect [0.5 0.5 1.0]}
             (model.block/closest-intersection block-pos arm))))
    ;; From the back -z
    (let [arm [[0.5 0.5 -0.5] [0.5 0.5 1.5]]]
      (is (= {:face [[0.0 0.0 0.0] [1.0 1.0 0.0]]
              :intersect [0.5 0.5 0.0]}
             (model.block/closest-intersection block-pos arm))))
    ;; From the top +y
    (let [arm [[0.5 1.5 0.5] [0.5 -0.5 0.5]]]
      (is (= {:face [[0.0 1.0 0.0] [1.0 1.0 1.0]]
              :intersect [0.5 1.0 0.5]}
             (model.block/closest-intersection block-pos arm))))
    ;; From the bottom -y
    (let [arm [[0.5 -0.5 0.5] [0.5 1.5 0.5]]]
      (is (= {:face [[0.0 0.0 0.0] [1.0 0.0 1.0]]
              :intersect [0.5 0.0 0.5]}
             (model.block/closest-intersection block-pos arm))))
    ;; From the +x side
    (let [arm [[1.5 0.5 0.5] [-0.5 0.5 0.5]]]
      (is (= {:face [[1.0 0.0 0.0] [1.0 1.0 1.0]]
              :intersect [1.0 0.5 0.5]}
             (model.block/closest-intersection block-pos arm))))
    ;; From the -x side
    (let [arm [[-0.5 0.5 0.5] [1.5 0.5 0.5]]]
      (is (= {:face [[0.0 0.0 0.0] [0.0 1.0 1.0]]
              :intersect [0.0 0.5 0.5]}
             (model.block/closest-intersection block-pos arm)))))
  ;; Translating the block-pos and arm by the same vector should have
  ;; no effect on the return value
  (let [block-pos [-0.5 -0.5 -0.5]
        arm [[-0.5 -0.5 0.5] [-0.5 -0.5 -1.5]]]
    ;; From the front +z
    (is (= {:face [[0.0 0.0 1.0] [1.0 1.0 1.0]]
            :intersect [-0.5 -0.5 0.0]}
           (model.block/closest-intersection block-pos arm)))))

(deftest faces-intersects-test
  (let [block-pos [0.5 0.5 0.5]]))
