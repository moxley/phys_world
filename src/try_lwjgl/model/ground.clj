(ns try-lwjgl.model.ground
  (:import [org.lwjgl.opengl GL11]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl.util.glu Sphere])
  (:require [try-lwjgl.display.util :as util]
            [try-lwjgl.physics :as physics]
            [try-lwjgl.shader :as shader]
            [try-lwjgl.model.textured-panel :as textured-panel]))

(defn gl-translatef [x y z] (GL11/glTranslatef x y z))

(defn create [world position]
  (let [phys-ground (physics/build-ground position)
        specs {:position (atom position)
               :phys phys-ground
               :world world}]
    (.addRigidBody world phys-ground)
    specs))

(defn draw [ground]
  (let [phys-ground (:phys ground)
        pos (physics/get-position phys-ground)]
    (util/with-pushed-matrix
      (apply gl-translatef pos)
      (doseq [x (range -10 10)
              z (range -10 10)]
        (util/with-pushed-matrix
          (gl-translatef x 0 z)
          (GL11/glRotatef 90 1 0 0)
          (textured-panel/draw))))))
