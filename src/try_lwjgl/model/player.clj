(ns try-lwjgl.model.player
  (:import [org.lwjgl.opengl GL11]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl.input Mouse Keyboard]
           [org.lwjgl.util.glu Sphere])
  (:require [try-lwjgl.display.util :as util]
            [try-lwjgl.physics :as physics]
            [try-lwjgl.shader :as shader]
            [try-lwjgl.input :as input]
            [try-lwjgl.math :as math]))

(def player-radius (float 1))
(def movement-speed 0.003)
(def movement-force-factor 0.03)

(defn gl-translatef [x y z] (GL11/glTranslatef x y z))

(defn create [world position]
  (let [phys-player (physics/build-player player-radius position)
        specs {:position (atom position)
               :orientation (atom [0 180 0])
               :phys phys-player
               :world world}]
    (.addRigidBody world phys-player)
    specs))

(defn unit-movement [right? left? backward? forward? down? up?]
  "Movement vector from origin position, origin rotation"
  (let [x (+ (if right? -1 0) (if left? 1 0))
        y (+ (if down? -1 0) (if up? 1 0))
        z (+ (if backward? -1 0) (if forward? 1 0))
        _ (println "x:" x ", y:" y ", z:" z)
        v (let [v (math/jvec3f x y z)]
            (when (not (= v (math/jvec3f 0 0 0))) (.normalize v))
            [(.x v) (.y v) (.z v)])]
    v))

(defn rotate-vector [v angle]
  (let [[x0 y0 z0] v
        x (- (* x0 (Math/cos angle)) (* z0 (Math/sin angle)))
        y y0
        z (+ (* x0 (Math/sin angle)) (* z0 (Math/cos angle)))]
    [x y z]))

(defn movement [delta player]
  (let [forward?    (input/key-down? :w)
        backward?  (input/key-down? :s)
        left?  (input/key-down? :a)
        right? (input/key-down? :d)
        up? (input/key-down? :space)
        down? (input/key-down? :lshift)
        m-unit-vector (unit-movement right? left? backward? forward? down? up?)
        orientation (map (fn [d] (Math/toRadians d)) (deref (:orientation player)))
        [pitch yaw roll] orientation
        [mx my mz] (rotate-vector m-unit-vector yaw)
        [fx fy fz] (map (fn [v] (* v delta movement-force-factor)) [mx my mz])
        force (math/jvec3f fx fy fz)]
    ;; Move player phys
    (.applyDamping (:phys player) 0.5)
    (.applyCentralImpulse (:phys player) force)))

(defn orientation [delta player]
  (let [d-angle (/ (input/mouse-dx) 10.0)
        orientation (deref (:orientation player))
        [pitch yaw roll] orientation
        new-yaw (+ yaw d-angle)
        new-orientation [pitch new-yaw roll]]
    (swap! (:orientation player) (fn [_] new-orientation))))

(defn logic [delta player]
  (movement delta player)
  (orientation delta player))

(defn draw [player]
  (let [phys-player (:phys player)
        sphere (Sphere.)
        pos (physics/get-position phys-player)]
    (util/with-pushed-matrix
     (shader/with-program
       (apply gl-translatef pos)
       (.setDrawStyle sphere GLU/GLU_SILHOUETTE)
       (GL11/glColor4f 1 0 0 1)
       (.draw sphere player-radius 30 30)))
    (swap! (:position player) (fn [_] pos))))
