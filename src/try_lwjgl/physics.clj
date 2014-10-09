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

(defn build-world []
  (let [broadphase (DbvtBroadphase.)
        collisionConfiguration (DefaultCollisionConfiguration.)
        dispatcher (CollisionDispatcher. collisionConfiguration)
        solver     (SequentialImpulseConstraintSolver.)
        world      (DiscreteDynamicsWorld. dispatcher broadphase solver collisionConfiguration)]
    (.setGravity world (Vector3f. 0 (* -1 GRAVITY) 0))
    world))

(defn build-ground []
  (let [normal (Vector3f. 0 1 0) ; Direction plane is facing
        plane-constant 0.0       ; Padding thickness above plane
        groundShape (StaticPlaneShape. normal plane-constant)
        groundMotionState (DefaultMotionState. (Transform. (Matrix4f.
                                                            (Quat4f. 0 0 0 1)
                                                            (Vector3f. 0 0 0)
                                                            1.0)))
        groundBodyConstructionInfo (RigidBodyConstructionInfo. 0 groundMotionState groundShape (Vector3f. 0 0 0))
        _ (set! (.restitution groundBodyConstructionInfo) 0.25)]
    (RigidBody. groundBodyConstructionInfo)))

(defn build-ball []
  (let [ballShape (SphereShape. 1.0)
        default-ball-transform (Transform. (Matrix4f. (Quat4f. 0 0 0 1)
                                                      ;; Starting position
                                                      (Vector3f. 0 5 0)
                                                      1.0))
        ballMotionState (DefaultMotionState. default-ball-transform)
        ballInertia (Vector3f. 0 0 0)
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
                                                 (Vector3f. 0 5 10)
                                                 1.0))
        motion-state (DefaultMotionState. default-transform)
        inertia (Vector3f. 0 0 0)
        _ (.calculateLocalInertia shape 2.5 inertia)
        construction-info (RigidBodyConstructionInfo. 2.5 motion-state shape inertia)
        _ (set! (.restitution construction-info) 0.25)
        _ (set! (.angularDamping construction-info) 0.95)
        body (RigidBody. construction-info)
        _ (.setActivationState body CollisionObject/DISABLE_DEACTIVATION)]
    body))

(defn build-world-with-objects []
  (let [world (build-world)
        ground (build-ground)
        ball (build-ball)]
    (.addRigidBody world ground)
    (.addRigidBody world ball)
    {:world world :ball ball}))

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
