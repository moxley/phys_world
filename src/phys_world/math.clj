(ns phys-world.math
  (:import [javax.vecmath Matrix3f Matrix4f Quat4f Vector3f Vector4f AxisAngle4f]))

(defn rad [degrees]
  (Math/toRadians degrees))

(defn cos [degrees]
  (Math/cos (rad degrees)))

(defn sin [degrees]
  (Math/sin (rad degrees)))

(defn jvtov [vector]
  [(.x vector) (.y vector) (.z vector)])

(defn vector3 [x y z]
  (Vector3f. (float x) (float y) (float z)))

(defn jmatrix3f
  ([] (Matrix3f.))
  ([m00 m01 m02 m10 m11 m12 m20 m21 m22]
      (Matrix3f.
       (float m00) (float m01) (float m02)
       (float m10) (float m11) (float m12)
       (float m20) (float m21) (float m22))))

(defn jmatrix4f
  ([] (Matrix4f.))
  ([^Quat4f q, ^Vector3f v, m] (Matrix4f. q v m))
  ([m00 m01 m02 m03, m10 m11 m12 m13, m20 m21 m22 m23, m30 m31 m32 m33]
     (Matrix4f.
      (float m00) (float m01) (float m02) (float m03)
      (float m10) (float m11) (float m12) (float m13)
      (float m20) (float m21) (float m22) (float m23)
      (float m30) (float m31) (float m32) (float m33))))

(defn jvec3f [x y z] (Vector3f. (float x) (float y) (float z)))

(defn axis-angle
  ([] (AxisAngle4f.))
  ([^Quat4f q]
     (let [a (axis-angle)]
       (.set a q)
       a))
  ([x y z angle]
     (AxisAngle4f.
      (float x)
      (float y)
      (float z)
      (float angle))))

(defn quat
  ([] (Quat4f.))
  ([^AxisAngle4f a]
     (let [q (Quat4f.)]
       (.set q a)
       q)))

(defn quat-2-pyr [quat]
  (let [a (axis-angle quat)
        x (.x a)
        y (.y a)
        z (.z a)
        angle (.angle a)]
    [(float 0)
     angle
     (float 0)]))

(defn quat-2-euler-old [quat]
  (let [x (.x quat)
        y (.y quat)
        z (.z quat)
        w (.w quat)
        phi (Math/atan2
             (+ (* x z) (* y w))
             (- (* x w) (* y z)))
        theta (Math/acos
               (+ (* (float -1) (* x x))
                  (* (float -1) (* y y))
                  (* z z)
                  (* w w)))
        psi (Math/atan2
             (- (* x z)
                (* y w))
             (+ (* y z)
                (* x w)))]
    [phi theta psi]))

(defn add [pos distance]
  "Translate point v by vector distance"
  (map + pos distance))

(defn sub [pos distance]
  "Translate point v by negative vector distance"
  (map - pos distance))

(defn mul [a b]
  "Multiply two vectors"
  (map * a b))

(defn div [a b]
  "Divide two vectors"
  (map / a b))

(defn scale [v m]
  "Multiply vector v by scalar magnitude m"
  (map #(* % m) v))

(defn square [a]
  (* a a))

(defn sqrt [a]
  (Math/sqrt (double a)))

(defn dot [a b]
  (try
    (reduce + (map * a b))
    (catch Exception e
      (println "dot: a:" a ", b:" b)
      (throw e))))

(defn v-dist [v]
  "Vector distance"
  (sqrt (reduce + (map square v))))

(defn magnitude [v]
  "Vector magnitude/distance"
  (sqrt (reduce + (map square v))))

(defn distance [point-a point-b]
  "Distance between two points, in any number of dimensions"
  (v-dist (map - point-a point-b)))

(defn min-distance [point other-points]
  "From a subject point, determines the closest of the other points"
  (let [distances (map #(distance point %) other-points)]
    (apply min distances)))

(defn unit [v]
  "Calculate unit vector of the given vector"
  (let [m (magnitude v)]
    (if (= 0 m) 0
        (div v (repeat m)))))

(defn coefficient [e1 e2 e3 e4]
  (- (* e1 e4) (* e2 e3)))

(defn cross [a b]
  "Cross product of two 3D vectors"
  (let [[a1 a2 a3] a
        [b1 b2 b3] b
        x (coefficient a2 a3 b2 b3)
        y (coefficient a1 a3 b1 b3)
        z (coefficient a1 a2 b1 b2)
        vx (mul [1 0 0] (repeat x))
        vy (mul [0 1 0] (repeat y))
        vz (mul [0 0 1] (repeat z))]
    (add (sub vx vy) vz)))

(defn plane-normal [p1 p2 p3]
  "Given three different points on a plane forming a 'v' shape, with p1 being the middle vertice, and angle theta between p1p2 and p1p3 being positive, return the plane normal vector"
  (let [v1 (sub p2 p1)
        v2 (sub p3 p1)]
    (unit (cross v1 v2))))

(defn line-plane-intersect [line plane]
  (let [[line-point line-direction] (map vec line)
        [plane-point plane-normal] (map vec plane)
        line-dot-normal (dot line-direction plane-normal)]
    (if
        ;; If line-dot-normal is zero, then plane and line are parallel
        (= 0 line-dot-normal) nil
        ;; Else, there is intersection
        (add (mul (repeat (/ (dot
                                ;; (sub plane-point line-point)

                              (try
                                (sub plane-point line-point)
                                (catch Exception e
                                  (println "Exception in (sub plane-point line-point). plane-point:" plane-point ", line-point:" line-point)
                                  (throw e)))
                              plane-normal)
                             line-dot-normal))
                  line-direction)
             line-point))))

;; (let [line-point-1 [0 1 2]
;;       line-point-2 [1 0 0]
;;       line-direction (sub line-point-2 line-point-1)
;;       line [line-point-1 line-direction]
;;       plane-point [0 0 1]
;;       plane-normal [0 0 1]
;;       plane [plane-point plane-normal]
;;       intersect (line-plane-intersect line plane)]
;;   intersect)
