(ns phys-world.model.ball
  (:import [org.lwjgl.opengl GL11]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl.util.glu Sphere])
  (:require [phys-world.display.util :as util]
            [phys-world.physics :as physics]
            [phys-world.shader :as shader]))

(defn gl-translatef [x y z] (GL11/glTranslatef x y z))

(defn create [world radius position]
  (let [phys-ball (physics/build-ball radius position)
        specs {:radius (atom radius)
               :position (atom position)
               :phys phys-ball
               :world world}]
    (.addRigidBody world phys-ball)
    specs))

(defn draw [ball]
  (let [phys-ball (:phys ball)
        radius (deref (:radius ball))
        sphere (Sphere.)
        pos (physics/get-position phys-ball)]
    (util/with-pushed-matrix
     (shader/with-program
       (apply gl-translatef pos)
       (.setDrawStyle sphere GLU/GLU_SILHOUETTE)
       (GL11/glColor4f 0 0.6 0 1)
       (.draw sphere radius 30 30)))
    (swap! (:position ball) (fn [_] pos))))
