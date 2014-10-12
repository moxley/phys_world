(ns try-lwjgl.model.player
  (:import [org.lwjgl.opengl GL11]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl.util.glu Sphere])
  (:require [try-lwjgl.display.util :as util]
            [try-lwjgl.physics :as physics]
            [try-lwjgl.shader :as shader]))

(defn gl-translatef [x y z] (GL11/glTranslatef x y z))

(def player-radius (float 1))

(defn create [world position]
  (let [phys-player (physics/build-player player-radius position)
        specs {:position (atom position)
               :phys phys-player
               :world world}]
    (.addRigidBody world phys-player)
    specs))

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
