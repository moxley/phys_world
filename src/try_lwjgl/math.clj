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

(defn add [v distance]
  (let [jv (apply jvec3f v)
        jdistance (apply jvec3f distance)]
    (.add jv jdistance)
    (jvtov jv)))
