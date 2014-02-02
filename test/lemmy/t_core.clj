(ns lemmy.t-core
  (:use midje.sweet)
  (:use [lemmy.core]))

(facts "about `assess-hand`"
  (fact "It finds pairs"
    (:result (assess-hand '((:A :spades) (:2 :clubs) (:5 :diamonds) (:2 :hearts) (:K :clubs)))) => [:pair :2]
    (:result (assess-hand '((:3 :spades) (:4 :clubs) (:5 :diamonds) (:6 :hearts) (:6 :clubs)))) => [:pair :6])

  (fact "it finds three of a kind"
    (:result (assess-hand '((:9 :spades) (:2 :clubs) (:8 :spades) (:8 :clubs) (:8 :diamonds)))) => [:three :8]
    (:result (assess-hand '((:7 :hearts) (:J :hearts) (:5 :clubs) (:J :diamonds) (:J :clubs)))) => [:three :J])

  (fact "it finds four of a kind"
    (:result (assess-hand '((:8 :spades) (:2 :clubs) (:8 :hearts) (:8 :clubs) (:8 :diamonds)))) => [:four :8]
    (:result (assess-hand '((:J :spades) (:J :hearts) (:5 :clubs) (:J :diamonds) (:J :clubs)))) => [:four :J])

  (fact "it finds full house"
    (:result (assess-hand '((:2 :spades) (:2 :clubs) (:8 :spades) (:8 :clubs) (:8 :diamonds)))) => [:full-house :8 :2]
    (:result (assess-hand '((:K :hearts) (:J :hearts) (:K :clubs) (:J :diamonds) (:J :clubs)))) => [:full-house :J :K])

  (fact "it finds straights"
    (:result (assess-hand '((:5 :spades) (:3 :hearts) (:A :hearts) (:4 :clubs) (:2 :spades)))) => [:straight]
    (:result (assess-hand '((:10 :spades) (:J :hearts) (:Q :hearts) (:K :clubs) (:A :spades)))) => [:straight])

  (fact "it finds flushes"
    (:result (assess-hand '((:4 :spades) (:7 :spades) (:10 :spades) (:Q :spades) (:A :spades)))) => [:flush])

  (fact "it finds straight flushes"
    (:result (assess-hand '((:5 :spades) (:6 :spades) (:7 :spades) (:8 :spades) (:9 :spades)))) => [:straight :flush])
  )

(facts "about `parse-hand`"
  (fact "these should pass"
    (parse-hand "Ah Kh 10h 5h 2h") => '((:A :hearts) (:K :hearts) (:10 :hearts) (:5 :hearts) (:2 :hearts))
    (parse-hand "3s 6s 4c 7d 2h") => '((:3 :spades) (:6 :spades) (:4 :clubs) (:7 :diamonds) (:2 :hearts))
    (parse-hand "10c Jd Qs Kh As") => '((:10 :clubs) (:J :diamonds) (:Q :spades) (:K :hearts) (:A :spades)))

  (fact "these should fail"
    (parse-hand "Ah Kh 10h 5k 2h") => [:parse_error "tokenization failed" ]
    (parse-hand "10c 3s 5s 12s Ad") => [:parse_error "tokenization failed"]
    (parse-hand "10c 3s 5s 3v Ad") => [:parse_error "tokenization failed"]
    (parse-hand "10c3s5s 12dAd") =>  [:parse_error "please supply the line in the form #$ #$ #$ #$ #$"]))


