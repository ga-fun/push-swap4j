# push-swap4j
A java implementation of the push-swap problem


## TODO

- [x] Add a message when no convergence is found
- [x] Optimize the search for convergence (stop always computing the same states)
- [x] Fix issues with the skip diff button when convergence is reached at the end of the move lists (error is thrown)
- [x] Be tolerant when saved list and index are inconsistent
- [x] Fix test suite
- [x] Fix refresh issue when moving in the move list selection (best/worse/neutral is lost).
- [x] Fix trunc mode crash when adding a move at the end of the list
- [-] Some additing related materials should be migrated from pushSwapSim to listView.
  - [x] current index moved to tooltip
  - [x] count moved in title bar
  - [x] edit mode
  - [x] title bar (with copy-clear buttons)
  - [ ] addMove method
- [ ] Position is not restored on reload.
- [ ] Diff selection should be cleared when move list is edited.
- [ ] Input (numbers or moves) should be verified and rejected if invalid.
- [ ] Typing should be forbidden in move fields (maybe a paste button should be added). One can corrupt the list by typing and there's Chrome typo warnings when clicking on a move.
- [ ] Fix issues in move list scroll when adding/removing moves
- [ ] Auto-play speed should be saved/reloaded
- [ ] Should we keep a 200 moves limit on convergence finder?