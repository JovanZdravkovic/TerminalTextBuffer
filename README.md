# TerminalTextBuffer
This solution models the screen as a fixed-size array of cells and the scrollback buffer as a ring buffer.

The scrollback buffer is implemented as a ring buffer with an underlying fixed-size array. This data structure was chosen because it provides O(1) write operations per cell and avoids additional dynamic allocations after initialization, in contrast to data structures like ArrayList and LinkedList. Both the screen and the scrollback buffer are implemented as flat one-dimensional arrays rather than two-dimensional matrices. This simplifies index management and allows cursor movement and position tracking to be handled with a single linear index, avoiding repeated row-wrapping logic.

Screen and scrollback contents are converted to strings by traversing the arrays when requested. The trade-off here is between:
- On-demand traversal (current approach): simpler and less error-prone.
- Maintaining a live string representation: potentially faster reads but significantly more complex and harder to keep consistent during insertions, deletions, scrolling, and resizing.

Text styles are represented using an enum and stored in an EnumSet. This approach was chosen because EnumSet is a specialized, memory-efficient implementation that internally behaves similarly to a bit-flag representation, while remaining maintainable.
