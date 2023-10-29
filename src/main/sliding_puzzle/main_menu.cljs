(ns sliding-puzzle.main-menu
  (:require
   [quil.core :as q]
   [sliding-puzzle.buttons :as buttons]
   [sliding-puzzle.level-selection :as level-selection]
   [sliding-puzzle.levels :as levels]
   [sliding-puzzle.sketch-functions :refer [add-derived-state draw-state
                                            key-pressed mouse-pressed]]
   [sliding-puzzle.screen-transition :as screen-transition]))

(defn mk-state [canvas-size]
  (add-derived-state
   {:type :main-menu
    :canvas-size canvas-size}))

(defmethod add-derived-state :main-menu
  [{:keys [canvas-size] :as state}]
  (assoc state
         :buttons
         [{:rect (let [p #(* canvas-size %)]
                   (map p [0.3 0.45 0.4 0.1]))
           :fn (fn [state] (screen-transition/mk-state
                            state
                            (first levels/levels)
                            :right))
           :text "Play"
           :text-size (* canvas-size 0.04)}
          {:rect (let [p #(* canvas-size %)]
                   (map p [0.3 0.6 0.4 0.1]))
           :fn (fn [state]
                 (screen-transition/mk-state state
                                             (level-selection/mk-state (:canvas-size state)
                                                                       state)
                                             :right))
           :text "Level Selection"
           :text-size (* canvas-size 0.04)}
          {:rect (let [p #(* canvas-size %)]
                   (map p [0.3 0.75 0.4 0.1]))
           :fn (fn [state]
                 (set! js/window.location.href "https://puzzling.stackexchange.com/users/62537/dmitry-kamenetsky")
                 state)
           :text "Puzzle Credit"
           :text-size (* canvas-size 0.04)}]))

(defmethod draw-state :main-menu
  [{:keys [canvas-size] :as state}]
  (q/fill 50)
  (q/no-stroke)
  (q/text-size (/ canvas-size 8))
  (q/text-align :center :center)
  (q/text "Sliding Puzzle" (* canvas-size 0.5) (* canvas-size 0.3))
  (q/text-size (* canvas-size 0.04))
  (q/text-align :left :center)
  (q/text "A game programmed by Oli Solomons"
          (* canvas-size 0.05) (* canvas-size 0.9))
  (q/text "Puzzles created by Dmitry Kamenetsky"
          (* canvas-size 0.05) (* canvas-size 0.95))
  (buttons/draw-buttons state))

(defmethod mouse-pressed :main-menu
  [state {:keys [x y]}]
  (if-let [button-fn (buttons/get-clicked-button-fn state x y)]
    (button-fn state)
    state))

(defmethod key-pressed :main-menu
  [state event]
  (case (:key event)
    :space (add-derived-state
            (assoc (first levels/levels)
                   :canvas-size (:canvas-size state)))
    state))

