# push-swap4j
A java implementation of the push-swap problem


## TODO

- [x] Add a message when no convergence is found
- [x] Optimize the search for convergence (stop computing again and again the same states)
- [x] Fix issues with the skip diff button when convergence is reached at the end of the move lists (error is thrown)
- [x] Be tolerant when saved list and index are inconsistent
- [x] Fix test suite
- [x] Fix refresh issue when moving in the move list selection (best/worse/neutral is lost).
- [x] Fix trunc mode crash when adding a move at the end of the list
- [x] Some editing related materials should be migrated from pushSwapSim to listView.
  - [x] current index moved to tooltip
  - [x] count moved in title bar
  - [x] edit mode
  - [x] title bar (with copy-clear buttons)
  - [x] addMove/deleteMove methods
- [x] Auto-play speed should be saved/reloaded
- [x] Fix play button behavior (respect the rate setting, even on large stacks).
- [x] Fix top bar buttons are not grayed out when they should be.
- [x] Input (numbers or moves) should be verified and rejected if invalid.
  - [x] Check move paste.
  - [x] Check number paste.
- [x] Typing should be forbidden in move fields (maybe a paste button should be added). One can corrupt the list by typing and there's Chrome typo warnings when clicking on a move.
- [x] Fix issues in move list scroll when adding/removing moves
- [x] Remove the 200 moves limit on convergence finder?
- [x] When start of find convergence zone are different, copy button should not be visible

## Known bug
Sometimes, during a synchronized fast-forward, the progress of the lists can become slightly out of sync.

This happens because the two animations are asynchronous. Each frame is calculated based on the last progress in the move list and the time elapsed since the last frame was displayed. Since this calculation doesn't occur at precisely the same time in both lists, the progress may be slightly different.