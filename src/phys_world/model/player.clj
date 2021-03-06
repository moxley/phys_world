(ns phys-world.model.player
  (:import [org.lwjgl.opengl GL11]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl.input Mouse Keyboard]
           [org.lwjgl.util.glu Sphere])
  (:require [phys-world.display.util :as util]
            [phys-world.physics :as physics]
            [phys-world.shader :as shader]
            [phys-world.input :as input]
            [phys-world.math :as math]))

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

(defn unit-movement [right? left? down? up? backward? forward?]
  "Movement vector from origin position, origin rotation"
  (let [x (+ (if right? -1 0) (if left? 1 0))
        y (+ (if down? -1 0) (if up? 1 0))
        z (+ (if backward? -1 0) (if forward? 1 0))
        v (let [v (math/jvec3f x y z)]
            (when (not (= v (math/jvec3f 0 0 0))) (.normalize v))
            [(.x v) (.y v) (.z v)])]
    v))

(defn rotate-vector [p angle]
  (let [[x0 y0 z0] p
        x (- (* x0 (Math/cos angle)) (* z0 (Math/sin angle)))
        y y0
        z (+ (* x0 (Math/sin angle)) (* z0 (Math/cos angle)))]
    [x y z]))

(defn forward-position
  ([player] (forward-position (physics/get-position (:phys player))
                               @(:orientation player)))
  ([pos orientation]
     (let [[px py pz] pos
           [pitch yaw roll] (map (fn [a] (Math/toRadians a)) orientation)
           ny (* -1 (Math/sin pitch))
           nx (* -1 (Math/sin yaw) (Math/cos pitch))
           nz (* (Math/cos yaw) (Math/cos pitch))
           npos0 (math/scale [nx ny nz] 2.0)
           [mx my mz] npos0
           x (+ px mx)
           y (+ py my)
           z (+ pz mz)]
       [x y z])))

(defn position [player]
  (physics/get-position (:phys player)))

(defn arm [player]
  [(position player) (forward-position player)])

(defn movement [delta player]
  (let [forward?    (input/key-down? :w)
        backward?  (input/key-down? :s)
        left?  (input/key-down? :a)
        right? (input/key-down? :d)
        up? (input/key-down? :space)
        down? (input/key-down? :lshift)
        m-unit-vector (unit-movement right? left? down? up? backward? forward?)
        orientation (map (fn [d] (Math/toRadians d)) (deref (:orientation player)))
        [pitch yaw roll] orientation
        [mx my mz] (rotate-vector m-unit-vector yaw)
        [fx fy fz] (map (fn [v] (* v delta movement-force-factor)) [mx my mz])
        force (math/jvec3f fx fy fz)]
    ;; Move player phys
    (.applyDamping (:phys player) 0.5)
    (.applyCentralImpulse (:phys player) force)))

(defn orientation [delta player]
  (let [mouse-factor 0.10
        dx (* (input/mouse-dx) mouse-factor)
        dy (* -1 (input/mouse-dy) mouse-factor)
        orientation (deref (:orientation player))
        [pitch yaw roll] orientation
        new-pitch (+ pitch dy)
        new-yaw (+ yaw dx)
        new-orientation [new-pitch new-yaw roll]]
    (swap! (:orientation player) (fn [_] new-orientation))))

(defn draw-sphere [pos]
  (let [sphere (Sphere.)]
    (util/with-pushed-matrix
      (shader/with-program
        (apply gl-translatef pos)
        (.setDrawStyle sphere GLU/GLU_SILHOUETTE)
        (GL11/glColor4f 1 0 0 1)
        (.draw sphere player-radius 30 30)))))

(defn draw [player]
  (let [phys-player (:phys player)
        pos (physics/get-position phys-player)]
    (when false (draw-sphere pos))))
