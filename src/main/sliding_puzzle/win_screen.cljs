(ns sliding-puzzle.win-screen
  (:require
   [quil.core :as q]
   [sliding-puzzle.buttons :as buttons]
   [sliding-puzzle.sketch-functions :refer [add-derived-state draw-state
                                            key-pressed mouse-dragged mouse-pressed
                                            update-state]]))

(def button-width 0.25)
(def buttom-height 0.1)
(def button-margin 0.04)

(def margin 20)

(defn mk-state [game-state]
  (add-derived-state
   {:type :win-screen
    :game-state game-state
    :canvas-size (:canvas-size game-state)}))

(defmethod add-derived-state :win-screen
  [{:keys [canvas-size game-state] :as state}]
  (assoc state
         :buttons
         (remove
          nil?
          [{:rect (let [p #(* canvas-size %)
                        width-height (- (* 0.8 canvas-size) (* 2 margin))]
                    [(+ (p 0.5) (* width-height -0.5) (p button-margin))
                     (+ (p 0.5) (* width-height 0.5) (p (* buttom-height -1)) (- (p button-margin)))
                     (p button-width) (p buttom-height)])
            :fn (fn [state] (add-derived-state
                             {:type :main-menu
                              :canvas-size (:canvas-size state)}))
            :text "Main Menu"
            :text-size (* canvas-size 0.04)}
           (when (:next-level game-state)
             {:rect (let [p #(* canvas-size %)
                          width-height (- (* 0.8 canvas-size) (* 2 margin))]
                      [(+ (p 0.5) (* width-height 0.5) (- (p button-width)) (- (p button-margin)))
                       (+ (p 0.5) (* width-height 0.5) (p (* buttom-height -1)) (- (p button-margin)))
                       (p button-width) (p buttom-height)])
              :fn (fn [state]
                    (add-derived-state
                     (assoc (:next-level game-state)
                            :canvas-size (:canvas-size state))))
              :text "Next Level"
              :text-size (* canvas-size 0.04)})])
         :game-state
         (add-derived-state
          (assoc game-state
                 :canvas-size canvas-size))))

(defmethod mouse-dragged :win-screen
  [state]
  state)

(defmethod update-state :win-screen
  [state]
  state)

(defmethod draw-state :win-screen
  [{:keys [canvas-size game-state] :as state}]
  (draw-state game-state)
  (q/stroke 60)
  (q/fill 80 30 230 230)
  (let [top-left (+ margin (* 0.1 canvas-size))
        width-height (- (* 0.8 canvas-size) (* 2 margin))]
    (q/rect top-left top-left width-height width-height (* 0.05 canvas-size)))
  (q/no-stroke)
  (q/fill 50)
  (q/text-size (* 0.06 canvas-size))
  (q/text-align :center :center)
  (q/text (if (:next-level game-state)
            "Yay! Sucess!"
            "You beat all the levels!")
          (* canvas-size 0.5)
          (* canvas-size 0.5))
  (buttons/draw-buttons state))

(defmethod mouse-pressed :win-screen
  [state {:keys [x y]}]
  (if-let [button-fn (buttons/get-clicked-button-fn state x y)]
    (button-fn state)
    state))

(defmethod key-pressed :win-screen
  [state event]
  (case (:key event)
    :space state
    state))

