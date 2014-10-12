(ns try-lwjgl.math
  (:import [javax.vecmath Matrix4f Quat4f Vector3f Vector4f]))

(defn rad [degrees]
  (Math/toRadians degrees))

(defn cos [degrees]
  (Math/cos (rad degrees)))

(defn sin [degrees]
  (Math/sin (rad degrees)))

(defn jvtov [vector]
  [(.x vector) (.y vector) (.z vector)])

(defn jmatrix4f [q v m] (Matrix4f. q v m))

(defn jvec3f [x y z] (Vector3f. (float x) (float y) (float z)))

(defn scale [v m]
  (let [jv (apply jvec3f v)]
    (.scale jv (float m))
    (jvtov jv)))

(defn add [v distance]
  (let [jv (apply jvec3f v)
        jdistance (apply jvec3f distance)]
    (.add jv jdistance)
    (jvtov jv)))
