package com.genomeRing.presenter.optimize;

import com.genomeRing.model.structure.CoveredBlock;
import com.genomeRing.model.structure.Genome;
import com.genomeRing.model.structure.RingDimensions;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is used to simulate drawing the GenomeRing and calculating the costs without actually having to create the GenomeRingView
 * again.
 * The code is taken from the view.GenomePathView.java class without the code used to draw the paths.
 */
public class Costs{
    protected static boolean ONLY_CLOCKWISE = false;
    public final static int CONNECTOR_ALPHA = 64;

    // compute the "price" of this path
    protected double cost_angles;
    protected int cost_blocks_jumped;
    protected int cost_jumps;


    public Costs(Genome genome, RingDimensions ringDimensions) {


        int gidx = genome.getIndex();
        List<CoveredBlock> blocks = genome.getBlocks(); // blocks sorted in order of genome

        // find consecutive blocks with same direction (clockwise, counterclockwise) to maximize number of direct connections

        List<ConsecutiveBlocks> groups = new ArrayList<ConsecutiveBlocks>();
        ConsecutiveBlocks group = new ConsecutiveBlocks(ringDimensions);
        groups.add(group);

        for (int i=0; i!=blocks.size(); ++i) {
            CoveredBlock b = blocks.get(i);
            if (!group.add(b)) {
                groups.add(group = new ConsecutiveBlocks(ringDimensions));
                group.add(b);
            }
        }
//
//		System.out.println(g.getIndex());
//		for (ConsecutiveBlocks cb : groups)
//			System.out.println(cb);

        // now, when drawing blocks, keep direction as long as possible.
        // single blocks with direction==0 use direction of previous consecutive blocks

        java.util.Iterator<ConsecutiveBlocks> groupIter = groups.iterator();
        group = groupIter.next(); // there is at least one group
        int groupOffset=0;
        int currentDirection=group.getDirection();
        int lastDirection = currentDirection;

        for (int blockIndex=0; blockIndex!=blocks.size()-1; ++blockIndex) {
            CoveredBlock b = blocks.get(blockIndex);
            CoveredBlock nextb = blocks.get(blockIndex+1);


            lastDirection = currentDirection;

            ++groupOffset;
            if (groupOffset==group.size()) {
                group = groupIter.next(); // check to see if direction needs change
                if (group.getDirection()!=0) {
                    currentDirection=group.getDirection();
                } else if (!ONLY_CLOCKWISE){
                    // no preference, use shorter angle
                    double alpha_end = lastDirection==1?ringDimensions.getEndDegree(b):ringDimensions.getStartDegree(b);
                    double alpha_next_start = currentDirection==1?ringDimensions.getStartDegree(nextb):ringDimensions.getEndDegree(nextb);
                    double alpha_dist = (alpha_end-alpha_next_start);
                    alpha_dist += 3600;
                    alpha_dist %= 360;
                    currentDirection = alpha_dist < 180 ?1:-1;

                    if (lastDirection==0 && blockIndex==0) {
                        // use best fit for first block if no direction given
                        lastDirection = currentDirection;
                    }

                }
                groupOffset=0;
            }

            if (lastDirection==0)
                lastDirection=1;

            // add block

            // add block connection.
            addConnector(b, nextb, ringDimensions, gidx, lastDirection, currentDirection);

            // store info for flags
            b.setDrawnClockwise(lastDirection==1);

        }
        // add last block (jump is already there)
        CoveredBlock lastBlock = blocks.get(blocks.size()-1);
         //addBlock(lastBlock, ringDimensions, gidx, currentDirection, true);

        lastBlock.setDrawnClockwise(currentDirection==1);

        // if genome is circular: add final jump
        if (genome.isCircular()) {
            if (ONLY_CLOCKWISE) {
                currentDirection=1;
            } else {
                groupIter = groups.iterator();
                while (groupIter.hasNext() && ((currentDirection=groupIter.next().getDirection())==0));
                if (currentDirection==0)
                    currentDirection=1;
            }
            addConnector(lastBlock, blocks.get(0), ringDimensions, gidx, lastDirection, currentDirection);
            //  closePath();
        }

//		System.out.println("Genome "+g.getName()+"\tJumps="+cost_jumps+"\tBlocks="+cost_blocks_jumped+"\tAngles="+cost_angles);
    }

    public double[] getCosts() {
        return new double[]{cost_jumps,cost_blocks_jumped,cost_angles};
    }

    protected void addJumpCost(int blocks, double angle) {
        cost_jumps++;
        cost_blocks_jumped+=blocks;
        cost_angles+=angle;
//		System.out.println("Added block cost "+blocks+" \tAdded angular cost "+angle);
    }



    private void addConnector(CoveredBlock b, CoveredBlock nextb, RingDimensions ringdim, int gidx, int lastDirection, int direction) {
//		System.out.println("Connecting Block with directions "+lastDirection+" -> "+direction+": "+nextb);

        double segment_radius = b.isForward()?ringdim.getRadiusForward(gidx):ringdim.getRadiusBackward(gidx);

        int NoB = ringdim.getNumberOfBlocks();
        // -1 % k == -1  UND NICHT: k-1
        boolean neighbours = ((NoB+b.getIndex()+direction) % NoB) ==nextb.getIndex();
        neighbours &= lastDirection==direction;

        double alpha_end = lastDirection==1?ringdim.getEndDegree(b):ringdim.getStartDegree(b);
        double alpha_next_start = direction==1?ringdim.getStartDegree(nextb):ringdim.getEndDegree(nextb);

        if (direction==1) {
            while (alpha_end<alpha_next_start)
                alpha_end+=360;
        } else {
            while (alpha_next_start<alpha_end)
                alpha_next_start+=360;
        }


        // cost computation
        double angle = Math.abs(alpha_next_start-alpha_end);
        if (angle>180 && !ONLY_CLOCKWISE)
            angle=360-angle; // reverse direction
        int blocks_jumped = nextb.getIndex()-b.getIndex();
        if (blocks_jumped<0)
            blocks_jumped += ringdim.getNumberOfBlocks(); // wrap around
        blocks_jumped--; // neighbor is free

        // five cases: direct same, direct switch, jump inner, jump outer, jump between
        if (neighbours) { // direct connection
            if (b.isForward()==nextb.isForward()) { // stay on same circle --COST:none

            } else { // switch circles directly
                // nothing to do, will be automatically connected to next segment by path rendering
                //--COST:none in terms of angles, one in terms of jump
                addJumpCost(0, 0);
            }
        } else { // jump needed: outer / exchange / inner
            if (b.isForward() && nextb.isForward()) { // outer jump
//				System.out.println("Block "+b.getBlock().getName()+" to "+nextb.getBlock().getName());
                int turn = addOuterJump(gidx,
                        segment_radius,
                        alpha_end, alpha_next_start,
                        ringdim.getBlockGap(),
                        lastDirection,
                        direction,
                        ringdim);
//				System.out.print(gidx+"outer "+turn+"    \t");
                if (turn==-1)
                    blocks_jumped=ringdim.getNumberOfBlocks()-blocks_jumped-2;
                addJumpCost(blocks_jumped, angle);
            } else if (b.isForward()!=nextb.isForward()) { // exchange jump
                double asrc = alpha_end-lastDirection*ringdim.getBlockGap()*0.75;
                double atgt = alpha_next_start+direction*ringdim.getBlockGap()*0.75;
                int turn = addInterchangeJump(gidx, ringdim, asrc, atgt);
                // interchange jumps can go backwards.
//				System.out.print(gidx+"interchange"+turn+"\t");
                if (turn==-1)
                    blocks_jumped=ringdim.getNumberOfBlocks()-blocks_jumped-2;
                addJumpCost(blocks_jumped, angle);
            } else { // inner jump
                // inner jump always is a direct jump, i.e. always cheapest
//				System.out.print(gidx+"inner         \t");
                addJumpCost(blocks_jumped, angle);
            }
        }
    }


    /**
     * @return direction of the jump
     */
    private int addOuterJump(
            int gidx, double radius_segment,
            double alpha_start, double alpha_end, double gap,
            int lastDirection, int direction,
            RingDimensions rd) {

        if (!ONLY_CLOCKWISE) {
            double as = alpha_start;
            double ae = alpha_end;
            while(as<0 || ae<0) {
                as+=360;
                ae+=360;
            }

            if (as-ae>180) {
                ae+=360;
            } else if (as-ae<-180) {
                as+=360;
            }

            alpha_start=as;
            alpha_end=ae;
        }

        int turn = alpha_start>alpha_end ? 1:-1;

        return turn;
    }


    /** @return direction of the jump */
    private int addInterchangeJump(int gidx, RingDimensions ringdim, double alpha_start, double alpha_end) {
        // jump to interchange level: done automatically by path rendering
        // draw segment on interchange level
        int turn=1;


        return turn;
    }



    private class ConsecutiveBlocks extends ArrayList<CoveredBlock> {
        int lastIndex=-1;
        int direction=0;
        RingDimensions rd;

        public ConsecutiveBlocks(RingDimensions rd) {
            this.rd=rd;
        }

        public boolean add(CoveredBlock b) {
            if (lastIndex!=-1) {

                if (get(size()-1).isForward()!=b.isForward())
                    return false;

                int indexDifference = b.getBlock().getIndex()-lastIndex;
                // special case: one block is at end of supergenome, one at start
                if (Math.abs(indexDifference)==rd.getNumberOfBlocks()-1) {
                    indexDifference = -(int)Math.signum(indexDifference);
                }

                // first check if this block is in fact consecutive to the existing ones
                if (Math.abs(indexDifference)>1) {
                    // it is not
                    return false;
                }
                if (direction==0) { // establish direction for the first time
                    direction=indexDifference;
                } else {
                    if (direction!=indexDifference) { // consecutive, but change of direction
                        throw new RuntimeException("Direction changes within consecutive blocks! One block may be added twice\n"+toString());
                    }
                }
            }
            super.add(b);
            lastIndex = b.getIndex();
            return true;
        }
        public int getDirection() {
            if (ONLY_CLOCKWISE)
                return 1;
            return direction;
        }
        public String toString() {
            return (direction==1?"CW ":(direction==-1?"CCW":"---"))+":"+super.toString();
        }
    }
}