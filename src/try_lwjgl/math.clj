(ns try-lwjgl.math
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

(defn scale [v m]
  (let [jv (apply jvec3f v)]
    (.scale jv (float m))
    (jvtov jv)))

(defn add [pos distance]
  "Translate point v by vector distance"
  (let [jpos (apply jvec3f pos)
        jdistance (apply jvec3f distance)]
    (.add jpos jdistance)
    (jvtov jpos)))

(defn sub [pos distance]
  "Translate point v by negative vector distance"
  (let [jpos (apply jvec3f pos)
        jdistance (apply jvec3f distance)]
    (.sub jpos jdistance)
    (jvtov jpos)))

(defn scale [pos distance]
  "Multiply point v by scalar distance"
  (let [jpos (apply jvec3f pos)]
    (.scale jpos distance)
    (jvtov jpos)))

(defn pythagorean-dist [a b]
  (Math/sqrt (+ a b)))

(defn distance-2d [p1 p2]
  (let [[p1x p1y] p1
        [p2x p2y] p2]
    (pythagorean-dist (Math/abs (float (- p2x p1x)))
                      (Math/abs (float (- p2y p1y))))))

(defn distance-3d [p1 p2]
  (let [[p1x p1y p1z] p1
        [p2x p2y p2z] p2
        xy-dist (distance-2d [p1x p1y] [p2x p2y])
        xz-dist (distance-2d [p1x p1z] [p2x p2z])]
    (pythagorean-dist xy-dist xz-dist)))

(defn closer-point-3d [point a b]
  (let [dist-a (distance-3d point a)
        dist-b (distance-3d point b)
        min-dist (min dist-a dist-b)]
    (if (= min-dist dist-a) a b)))
