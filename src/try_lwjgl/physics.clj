(ns try-lwjgl.physics
  (:import [com.bulletphysics.collision.broadphase BroadphaseInterface DbvtBroadphase]
           [com.bulletphysics.collision.dispatch CollisionConfiguration CollisionDispatcher CollisionObject DefaultCollisionConfiguration]
           [com.bulletphysics.collision.shapes CollisionShape SphereShape CapsuleShape StaticPlaneShape]
           [com.bulletphysics.dynamics DiscreteDynamicsWorld DynamicsWorld RigidBody RigidBodyConstructionInfo]
           [com.bulletphysics.dynamics.constraintsolver ConstraintSolver SequentialImpulseConstraintSolver]
           [com.bulletphysics.linearmath DefaultMotionState MotionState Transform]
           [org.lwjgl.util.glu GLU Sphere]
           [javax.vecmath Matrix4f Quat4f Vector3f Vector4f]))

(def GRAVITY 10)

(defn jmatrix4f [q v m] (Matrix4f. q v m))
(defn jvec3f [x y z] (Vector3f. (float x) (float y) (float z)))

(defn build-world []
  (let [broadphase (DbvtBroadphase.)
        collisionConfiguration (DefaultCollisionConfiguration.)
        dispatcher (CollisionDispatcher. collisionConfiguration)
        solver     (SequentialImpulseConstraintSolver.)
        world      (DiscreteDynamicsWorld. dispatcher broadphase solver collisionConfiguration)]
    (.setGravity world (jvec3f 0 (* -1 GRAVITY) 0))
    world))

(defn build-ground []
  (let [normal (jvec3f 0 1 0) ; Direction plane is facing
        plane-constant 0.0       ; Padding thickness above plane
        groundShape (StaticPlaneShape. normal plane-constant)
        groundMotionState (DefaultMotionState. (Transform. (jmatrix4f
                                                            (Quat4f. 0 0 0 1)
                                                            (jvec3f 0 0 0)
                                                            (float 1))))
        groundBodyConstructionInfo (RigidBodyConstructionInfo. 0 groundMotionState groundShape (jvec3f 0 0 0))
        _ (set! (.restitution groundBodyConstructionInfo) 0.25)]
    (RigidBody. groundBodyConstructionInfo)))

(defn build-ball [radius position]
  (let [ballShape (SphereShape. radius)
        p (vec (map #(float %) position))
        default-ball-transform (Transform. (Matrix4f. (Quat4f. 0 0 0 1)
                                                      ;; Starting
                                                      ;; position
                                                      (apply jvec3f p)
                                                      (float 1)))
        ballMotionState (DefaultMotionState. default-ball-transform)
        ballInertia (jvec3f 0 0 0)
        _ (.calculateLocalInertia ballShape 2.5 ballInertia)
        ballConstructionInfo (RigidBodyConstructionInfo. 2.5 ballMotionState ballShape ballInertia)
        _ (set! (.restitution ballConstructionInfo) 0.5)
        _ (set! (.angularDamping ballConstructionInfo) 0.95)
        ball (RigidBody. ballConstructionInfo)
        _ (.setActivationState ball CollisionObject/DISABLE_DEACTIVATION)]
    ball))

(defn build-player []
  (let [shape (CapsuleShape. 0.25 2.0)
        default-transform (Transform. (Matrix4f. (Quat4f. 0 0 0 1)
                                                 ;; Starting position
                                                 (jvec3f 0 5 10)
                                                 1.0))
        motion-state (DefaultMotionState. default-transform)
        inertia (jvec3f 0 0 0)
        _ (.calculateLocalInertia shape 2.5 inertia)
        construction-info (RigidBodyConstructionInfo. 2.5 motion-state shape inertia)
        _ (set! (.restitution construction-info) 0.25)
        _ (set! (.angularDamping construction-info) 0.95)
        body (RigidBody. construction-info)
        _ (.setActivationState body CollisionObject/DISABLE_DEACTIVATION)]
    body))

(defn build-world-with-objects []
  (let [world (build-world)
        ground (build-ground)]
    (.addRigidBody world ground)
    {:world world :ground ground}))

(defn get-position [body]
  (let [position (.origin (.getWorldTransform (.getMotionState body) (Transform.)))]
    [(.x position) (.y position) (.z position)]))

(defn run-simulation []
  (let [everything (build-world-with-objects)
        world (:world everything)
        ball (:ball everything)]
    (doseq [i (range 100)]
     (.stepSimulation world 0.1 20)
      (println "position:" (get-position ball))
      (Thread/sleep 50))))

(def world (atom nil))

(defn world-and-objects []
  (if @world
    @world
    (swap! world (fn [_] (build-world-with-objects)))))
