package org.openpredict.exchange.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpredict.exchange.beans.OrderAction;
import org.openpredict.exchange.beans.PortfolioPosition;
import org.openpredict.exchange.beans.SymbolPortfolio;
import org.openpredict.exchange.beans.UserProfile;
import org.openpredict.exchange.beans.cmd.OrderCommand;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioService {

    private final UserProfileService userProfileService;

    public void holdDepositForNewOrder(OrderCommand order, UserProfile userProfile) {
        //SymbolPortfolio portfolio = userProfile.portfolio.computeIfAbsent(order.symbol, SymbolPortfolio::new);
        SymbolPortfolio portfolio = userProfile.portfolio.get(order.symbol);
        if (portfolio == null) {
            portfolio = new SymbolPortfolio(order.symbol, userProfile.uid);
            userProfile.portfolio.put(order.symbol, portfolio);
        }

        if (order.action == OrderAction.ASK) {
            portfolio.pendingSellSize += order.size;
        } else {
            portfolio.pendingBuySize += order.size;
        }

//        if(userProfile.uid == 555){
//            log.debug("HOLD  {}", order);
//            log.debug("pendingSellSize={} pendingBuySize={}", portfolio.pendingSellSize, portfolio.pendingBuySize);
//        }
    }

    /**
     * Update portfolio for one user 1. Un-hold pending size 2. Reduce opposite position accordingly (if exists) 3. Increase forward position
     * accordingly (if size left in the trading event)
     */
    public void updatePortfolioForTrade(OrderAction action, long size, int price, SymbolPortfolio portfolio) {

        // 1. un-hold pending size
        if (action == OrderAction.ASK) {
            portfolio.pendingSellSize -= size;
        } else {
            portfolio.pendingBuySize -= size;
        }

//        if(cmd.uid == 555){
//            log.debug("TRADE  {}", cmd);
//            log.debug("pendingSellSize={} pendingBuySize={}", portfolio.pendingSellSize, portfolio.pendingBuySize);
//        }

        // TODO investigate why can be negative
//        if (portfolio.pendingSellSize < 0 || portfolio.pendingBuySize < 0) {
//            throw new IllegalStateException();
//        }

        long remainingVolume = size;

        // 2. Reduce opposite position accordingly (if exists)
        if (portfolio.position.isOppositeToAction(action)) {
            // action is opposite to existing position

            // remove position until position is empty or no trade size left (whichever comes first)
            while (remainingVolume != 0 && portfolio.portfolioHasElement()) {
                long portfRecordVol = portfolio.headVolume();
                if (portfRecordVol <= remainingVolume) {
                    // portfolio record is smaller than size left, can remove completely
                    portfolio.totalSize -= portfRecordVol;
                    portfolio.acquireAmountSum -= portfolio.headPrice() * portfRecordVol;
                    remainingVolume -= portfRecordVol;
                    portfolio.portfolioRemove();
                } else {
                    // portfolio record has bigger size than we need - reduce size partially
                    portfolio.reduceHeadVolume(remainingVolume);
                    portfolio.totalSize -= remainingVolume;
                    portfolio.acquireAmountSum -= portfolio.headPrice() * remainingVolume;
                    remainingVolume = 0;
                }
            }
        }

        if (remainingVolume == 0) {
            portfolio.position = PortfolioPosition.EMPTY;
            return;
        }

        if (remainingVolume < 0) {
            throw new IllegalStateException();
        }

        // 3. Increase forward position accordingly
        portfolio.position = PortfolioPosition.of(action);
        portfolio.totalSize += size;
        portfolio.acquireAmountSum += price;

        if (portfolio.portfolioHasElement() && portfolio.tailPrice() == price) {
            // just an optimization
            // if trading big amount with same price from different parties
            // small peaces glued together
            portfolio.incTailVolume(size);
        } else {
            portfolio.portfolioAdd(price, size);
        }

    }

    public void updatePortfolioForReduce(OrderAction action, long size, SymbolPortfolio portfolio) {

        // un-hold pending size
        if (action == OrderAction.ASK) {
            portfolio.pendingSellSize -= size;
        } else {
            portfolio.pendingBuySize -= size;
        }

//        if(portfolio.uid == 555){
//            log.debug("REDUCE/REJECT  {} {}", size, action);
//            log.debug("pendingSellSize={} pendingBuySize={}", portfolio.pendingSellSize, portfolio.pendingBuySize);
//        }

    }


}
