package simpledb.tx.recovery;

import simpledb.buffer.Buffer;
import simpledb.buffer.BufferMgr;
import simpledb.file.Block;
import simpledb.log.BasicLogRecord;
import simpledb.server.SimpleDB;

class SetStringRecord implements LogRecord {
	private int txnum, offset;
	private String oldVal, newVal;
	private Block blk;

	/**
	 * Creates a new setstring log record.
	 * 
	 * @param txnum
	 *            the ID of the specified transaction
	 * @param blk
	 *            the block containing the value
	 * @param offset
	 *            the offset of the value in the block
	 * @param oldVal,
	 *            String newVal the new value
	 */
	public SetStringRecord(int txnum, Block blk, int offset, String oldVal, String newVal) {
		this.txnum = txnum;
		this.blk = blk;
		this.offset = offset;
		this.oldVal = oldVal;
		this.newVal = newVal;

	}

	/**
	 * Creates a log record by reading five other values from the log.
	 * 
	 * @param rec
	 *            the basic log record
	 */
	public SetStringRecord(BasicLogRecord rec) {
		txnum = rec.nextInt();
		String filename = rec.nextString();
		int blknum = rec.nextInt();
		blk = new Block(filename, blknum);
		offset = rec.nextInt();
		oldVal = rec.nextString();
		newVal = rec.nextString();
	}

	/**
	 * Writes a setString record to the log. This log record contains the
	 * SETSTRING operator, followed by the transaction id, the filename, number,
	 * and offset of the modified block, and the previous string value at that
	 * offset.
	 * 
	 * @return the LSN of the last log value
	 */
	public int writeToLog() {
		Object[] rec = new Object[] { SETSTRING, txnum, blk.fileName(), blk.number(), offset, oldVal, newVal };
		//System.out.println(toString());
		return logMgr.append(rec);
	}

	public int op() {
		return SETSTRING;
	}

	public int txNumber() {
		return txnum;
	}

	public String toString() {
		return "<SETSTRING " + txnum + " " + blk.fileName() + " blocknum " + blk.number()+" " + offset + " " + oldVal + " || "+ newVal + ">";
	}

	/**
	 * Replaces the specified data value with the value saved in the log record.
	 * The method pins a buffer to the specified block, calls setString to
	 * restore the saved value (using a dummy LSN), and unpins the buffer.
	 * 
	 * @see simpledb.tx.recovery.LogRecord#undo(int)
	 */
	public void undo(int txnum) {
		BufferMgr buffMgr = SimpleDB.bufferMgr();
		Buffer buff = buffMgr.pin(blk);
		buff.setString(offset, oldVal, txnum, -1);
		buffMgr.unpin(buff);
	}
	
	public void redo(int txnum) {
		BufferMgr buffMgr = SimpleDB.bufferMgr();
		Buffer buff = buffMgr.pin(blk);
		buff.setString(offset, newVal, txnum, -1);
		buffMgr.unpin(buff);
	}
}