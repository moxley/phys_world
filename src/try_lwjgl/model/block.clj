(ns try-lwjgl.model.block
  (:import [org.lwjgl.opengl GL11])
  (:require [try-lwjgl.display.util :as util]
            [try-lwjgl.shader :as shader]
            [try-lwjgl.physics :as physics]
            [try-lwjgl.math :as math]
            [try-lwjgl.model.wood-block :as wood-block]))

(defn create [world position]
  (let [width 1
        phys (physics/build-box width position)
        specs {:width width
               :position position
               :phys phys
               :world world}]
    (.addRigidBody world phys)
    specs))

(defn create-many [world]
  (let [width 4
        height 3
        progression (for [y (range height) x (range width)] [x y])]
    (map (fn [i] (let [[side up] i] (create world [side up (* -1.0 up)])))
         progression)))

(defn position [block]
  (physics/get-position (:phys block)))

(defn graph-position [block]
  (let [pos (position block)
        [x y z] pos
        width (:width block)
        offset (/ width 2.0)]
    [(- x offset) (- y offset) (- z offset)]))

(defn draw-face
  ([block face]
     (draw-face (graph-position block) nil nil))

  ([graph-pos translate rotations]
     (let [[x y z] graph-pos]
       (shader/with-program
         (util/with-pushed-matrix
           (GL11/glTranslatef 0 0 0)
           (GL11/glTranslatef x y z)

           ;; White
           (GL11/glColor3f 0.8 0.8 0.8)
           (util/do-shape GL11/GL_LINES
                          (GL11/glVertex3f 0 0 0)
                          (GL11/glVertex3f 1 0 0)

                          (GL11/glVertex3f 1 0 0)
                          (GL11/glVertex3f 1 1 0)

                          (GL11/glVertex3f 1 1 0)
                          (GL11/glVertex3f 0 1 0)

                          (GL11/glVertex3f 0 1 0)
                          (GL11/glVertex3f 0 0 0)))))))

(defn draw [block]
  (let [graph-pos (graph-position block)
        [x y z] graph-pos]
    (util/with-pushed-matrix
      (GL11/glTranslatef x y z)
      (wood-block/draw))
    (draw-face block [[0 0 0] [1 1 0]])))

(defn draw-many [blocks]
  (doseq [block blocks]
    (draw block)))

(defn face-intersect [face pointer]
  ;; intersects in 2D (facing z)
  (let [[f1 f2] face
        [p1 p2] pointer
        [f1x f1y f1z] f1
        [f2x f2y f2z] f2
        [p1x p1y p1z] p1
        [p2x p2y p2z] p2
        iy (- p1y (+ f1y (/ (- f2y f1y) 2)))
        iz (- p1z (+ f1z (/ (- f2z f1z) 2)))]
    [iy iz])
  ;; intersects in 2D (facing y)
  )

;;(face-intersect [[0 0 0] [1 1 0]] [[0.5 0.5 0.5] [0.5 0.5 -0.5]])
(defn slope [line]
  (let [[p1 p2] line
        [p1x p1y] p1
        [p2x p2y] p2]
    (if (= p2x p1x)
      nil
      (/ (- p2y p1y) (- p2x p1x)))))

(defn slope-offset [seg]
  (let [[p1 p2] seg
        [p1x p1y] p1
        p-slope (slope seg)
        p-y-offset (and p-slope (+ p1y (* p-slope -1.0 p1x)))]
    [p-slope p-y-offset]))

(defn line-intersect [seg1 seg2]
  (let [[slope1 off1] (slope-offset seg1)
        [slope2 off2] (slope-offset seg2)]
    (cond
     (and slope1 slope2)
     (let [x (/ (- off2 off1) (- slope1 slope2))
           y (+ (* x slope1) off1)]
       [x y])

     (not slope1)
     (let [x ((seg1 0) 0)
           y (+ (* x slope2) off2)]
       [x y])

     (not slope2)
     (let [x ((seg2 0) 0)
           y (+ (* x slope1) off1)]
       [x y])

     :else
     nil)))

;;(line-intersect [[1.5 0.5] [1.5 -0.5]] [[0 0] [1 0]])
;; [1.5 0.0]

(defn rect-contains-point? [rect point]
  (let [min (fn [a b] (Math/min a b))
        max (fn [a b] (Math/max a b))
        [a1 a2] rect
        [a1x a1y] a1
        [a2x a2y] a2
        [px py] point]
    (and (>= px (min a1x a2x)) (<= px (max a1x a2x))
         (>= py (min a1y a2y)) (<= py (max a1y a2y)))))

(defn constrain-intersect [intersect seg1 seg2]
  (cond
   (not intersect)
   nil

   (and (rect-contains-point? seg1 intersect)
        (rect-contains-point? seg2 intersect))
   intersect

   :else
   nil))

;;(constrain-intersect [0.5 0 0] [[0.5 0.5] [0.5 -0.5]] [[0 0] [1 0]])

(defn line-segment-intersect [seg1 seg2]
  (let [intersect (line-intersect seg1 seg2)]
    (constrain-intersect intersect seg1 seg2)))

;(line-intersect [[0.5 0.5] [0.5 -0.5]] [[0 0] [1 0]])
;;(line-segment-intersect [[0.5 0.5] [0.5 -0.5]] [[0 0] [1 0]])

(defn arm-face-intersect [arm face]
  ;; facing down, towards y-axis
  ;; Get x-z coords from face points
  (let [[a1 a2] arm
        arm1-xz [(a1 0) (a1 2)]
        arm2-xz [(a2 0) (a2 2)]
        arm-xz-line [arm1-xz arm2-xz]
        arm1-xy [(a1 0) (a1 1)]
        arm2-xy [(a2 0) (a2 1)]
        arm-xy-line [arm1-xy arm2-xy]
        [f1 f2] face
        face1-xz [(f1 0) (f1 2)]
        face2-xz [(f2 0) (f2 2)]
        face-xz-line [face1-xz face2-xz]
        face1-xy [(f1 0) (f1 1)]
        face2-xy [(f2 0) (f2 1)]
        face-xy-line [face1-xy face2-xy]
        i-xz (line-segment-intersect arm-xz-line face-xz-line)
        i-xy (line-segment-intersect arm-xy-line face-xy-line)]
    (and i-xz
         i-xy
         [(i-xz 0) (i-xy 1) (i-xz 1)])))

;; (let [arm  [[0.5 0.5 0.5] [0.5 0.5 -0.5]]
;;       face [[0 0 0] [1 1 0]]]
;;   (arm-face-intersect arm face))

(defn faces [pos]
  "Location of faces for a given block"
  (let [[x y z] pos]
      [
       [[x y z] [(+ x 1) (+ y 1) z]]
       [[x y z] [(+ x 1) y (+ z 1)]]
       [[x y z] [x (+ y 1) (+ z 1)]]

       [[x y (+ z 1)] [(+ x 1) (+ y 1) (+ z 1)]]
       [[x (+ y 1) z] [(+ x 1) (+ y 1) (+ z 1)]]
       [[(+ x 1) y z] [(+ x 1) (+ y 1) (+ z 1)]]]))

(defn closest-intersecting-face [phys-pos p1 p2]
  (let [pos (math/add phys-pos [-0.5 -0.5 -0.5])]
    (faces pos)))

;;(closest-intersecting-face [0.5 0.5 0.5] [0.5 0.5 1.5] [0.5 0.5 0.5])

