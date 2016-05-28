(ns try-lwjgl.model.block
  (:import [org.lwjgl.opengl GL11]
           [com.bulletphysics.dynamics DiscreteDynamicsWorld DynamicsWorld RigidBody RigidBodyConstructionInfo])
  (:require [try-lwjgl.display.util :as util]
            [try-lwjgl.shader :as shader]
            [try-lwjgl.physics :as physics]
            [try-lwjgl.math :as math]
            [try-lwjgl.model.wood-block :as wood-block]))

(defn create [^DiscreteDynamicsWorld world position]
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
     (and (not slope1) (not slope2))
     nil

     (and slope1 slope2)
     (let [slope-diff (- slope1 slope2)]
       (if (= slope-diff 0)
         (let [x (/ (- off2 off1) slope-diff)
               y (+ (* x slope1) off1)]
           [x y]))
       nil)

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

(defn rect-contains-point? [rect point]
  (let [[a1 a2] rect
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

(defn find-third-face-vertice [vert1 vert2]
  "Given vertices of opposite corners of a block face, return a third vertice of the face"
  (let [;; Indices where vert1 and vert2 differ
        ;; Ex: vert1: [3 3 3 3], vert2: [3 3 4 5].
        ;;     They differ at indices 2 and 3: (false, false, 2, 3)
        indices-where-components-differ (map (fn [c1 c2 i] (and (not= c1 c2) i))
                                             vert1
                                             vert2
                                             (iterate inc 0))

        ;; The index of the first difference
        diff-at (first (filter identity indices-where-components-differ))
        vert3 (and diff-at (assoc (vec vert1) diff-at ((vec vert2) diff-at)))]
    vert3))

(defn face-normal [vert1 vert2]
  "Given vertices of opposite corners of block face, return the plane normal for the face"
  (let [vert3 (find-third-face-vertice vert1 vert2)]
    (math/plane-normal vert1 vert2 vert3)))

(defn arm-face-intersect [arm face-abs]
  (let [line-point (first arm)
        line-direction (apply math/sub arm)
        line [line-point line-direction]
        face-abs (vec (map vec face-abs))
        plane-point (vec (first face-abs))
        ;; plane-point (first face-abs)
        normal (vec (apply face-normal face-abs))
        ;; (try
        ;;   (vec (apply face-normal face-abs))
        ;;   (catch Exception e
        ;;     (println "Exception raised. arm:" arm ", face-abs:" face-abs)
        ;;     (throw e)))
        plane [plane-point normal]]
    (math/line-plane-intersect line plane)))

(def HALF_WIDTH (float 0.5))
(def HALF_WIDTH_VECTOR [HALF_WIDTH HALF_WIDTH HALF_WIDTH])

(defn face-abs [block-pos face-rel]
  (map #(math/add (math/sub block-pos HALF_WIDTH_VECTOR) %)
       face-rel))

(def FACES
  "Description of block faces, relative to origin (0 0 0)"
  ;; Back -z
  [[[0.0 0.0 0.0] [1.0 1.0 0.0]]
   ;; Bottom
   [[0.0 0.0 0.0] [1.0 0.0 1.0]]
   ;; Side -x
   [[0.0 0.0 0.0] [0.0 1.0 1.0]]
   ;; Front +z
   [[0.0 0.0 1.0] [1.0 1.0 1.0]]
   ;; Top +y
   [[0.0 1.0 0.0] [1.0 1.0 1.0]]
   ;; Side +x
   [[1.0 0.0 0.0] [1.0 1.0 1.0]]])

(defn arm-block-intersects
  ([arm block-pos] (arm-block-intersects arm block-pos FACES))
  ([arm block-pos FACES]
     (try
       (vec (map #(arm-face-intersect arm (face-abs block-pos %))
                 FACES))
       (catch Exception e
         (println "Exception in arm-block-intersects. arm:" arm ", block-pos:" block-pos)
         (throw e)))))

(defn closest-intersection [block-pos arm]
  (let [[p1 p2] arm
        intersects (arm-block-intersects arm block-pos FACES)
        faces-intersects (map #(let [[face intersect] %]
                                 {:face face :intersect intersect})
                              (partition 2 (interleave FACES intersects)))
        matching-faces-intersects (filter :intersect faces-intersects)
        closer (fn [fia fib] (cond
                             (and fia fib)
                             (let [a (:intersect fia)
                                   b (:intersect fib)
                                   cp (math/min-distance p1 [a b])]
                               (if (= cp a) fia fib))

                             fia fia
                             fib fib
                             :else nil))
        closest-fi (and (first matching-faces-intersects)
                        (try
                          (reduce closer nil matching-faces-intersects)
                          (catch Exception e
                            (println "Exception raised. matching-faces-intersects:" matching-faces-intersects)
                            (throw e))))
        ]
    closest-fi))

(defn graph-position [block]
  (let [pos (position block)
        [x y z] pos
        width (:width block)
        offset (/ width 2.0)]
    [(- x offset) (- y offset) (- z offset)]))

(defn draw-face [block-pos face]
  (let [[x y z] (math/sub block-pos HALF_WIDTH_VECTOR)]
    (shader/with-program
      (util/with-pushed-matrix
        (GL11/glTranslatef x y z)

        ;; White
        (GL11/glColor3f 0.8 0.8 0.8)
        (util/do-shape
         GL11/GL_LINES
         (doseq [[x y z] (apply util/rect-vertices face)]
           (GL11/glVertex3f x y z)))))))

(defn draw [block highlight-face]
  (let [graph-pos (graph-position block)
        [x y z] graph-pos]
    (util/with-pushed-matrix
      (GL11/glTranslatef x y z)
      (wood-block/draw))
    (when-let [f highlight-face]
      ;; (draw-face (position block) [[0 0 0] [1 1 0]])
      (draw-face (position block) (:face f)))))

(defn draw-many [blocks highlight-face]
  (doseq [block blocks]
    (draw block highlight-face)))
