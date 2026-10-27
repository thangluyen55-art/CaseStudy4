package com.yourname.digishop.service;

import com.yourname.digishop.entity.*;
import com.yourname.digishop.exception.BusinessException;
import com.yourname.digishop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeckService {
    public static final int MAIN_SIZE = 50;
    public static final int MAX_COPIES = 4;
    public static final int MAX_EGGS = 5;

    private final DeckRepository decks;
    private final CardRepository cards;
    private final UserRepository users;
    private final ProductRepository products;
    private final CartService cartService;

    public record AddResult(int added, List<String> shortages) {}

    @Transactional(readOnly = true)
    public List<Deck> list(String email) {
        return decks.findByUserEmailOrderByCreatedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public Deck get(Long id, String email) {
        Deck deck = decks.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bộ bài"));
        if (!deck.getUser().getEmail().equals(email))
            throw new BusinessException("Bạn không có quyền xem bộ bài này");
        return deck;
    }

    @Transactional(rollbackFor = Exception.class)
    public Deck create(String email, String name) {
        User u = users.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản"));
        Deck d = new Deck();
        d.setUser(u);
        d.setName(name == null || name.isBlank() ? "Bộ bài mới"
                : name.trim().substring(0, Math.min(name.trim().length(), 100)));
        return decks.save(d);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, String email) {
        decks.delete(get(id, email));
    }

    @Transactional(readOnly = true)
    public List<Card> searchCards(String q) {
        String key = q == null ? "" : q.trim();
        return cards.findTop24ByNameContainingIgnoreCaseOrCardCodeContainingIgnoreCase(key, key);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addCard(Long deckId, String email, Long cardId) {
        Deck deck = get(deckId, email);
        Card card = cards.findById(cardId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thẻ"));
        boolean egg = card.getCardType() == CardType.DIGI_EGG;

        DeckCard dc = find(deck, cardId);
        int current = dc == null ? 0 : dc.getQuantity();

        if (current >= MAX_COPIES)
            throw new BusinessException("Mỗi thẻ chỉ được tối đa " + MAX_COPIES + " bản trong một bộ bài");
        if (egg && deck.getEggCount() >= MAX_EGGS)
            throw new BusinessException("Chỉ được tối đa " + MAX_EGGS + " Digi-Egg");
        if (!egg && deck.getMainCount() >= MAIN_SIZE)
            throw new BusinessException("Bộ bài chính đã đủ " + MAIN_SIZE + " lá");

        if (dc == null) {
            dc = new DeckCard();
            dc.setDeck(deck);
            dc.setCard(card);
            deck.getCards().add(dc);
        }
        dc.setQuantity(current + 1);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeCard(Long deckId, String email, Long cardId) {
        Deck deck = get(deckId, email);
        DeckCard dc = find(deck, cardId);
        if (dc == null) return;
        if (dc.getQuantity() <= 1) deck.getCards().remove(dc);     // orphanRemoval tự xóa
        else dc.setQuantity(dc.getQuantity() - 1);
    }

    /**
     * Thêm cả bộ bài vào giỏ hàng. Mỗi thẻ ưu tiên bản tình trạng tốt nhất (giá cao nhất) còn hàng.
     * Không đánh dấu @Transactional: mỗi lần thêm vào giỏ là một giao dịch riêng,
     * nên một thẻ thiếu hàng không làm hỏng các thẻ khác.
     */
    public AddResult addToCart(Long deckId, String email) {
        Deck deck = get(deckId, email);
        int added = 0;
        List<String> shortages = new ArrayList<>();

        for (DeckCard dc : deck.getCards()) {
            int need = dc.getQuantity();
            List<Product> options = products
                    .findByCardIdAndActiveTrueAndStockGreaterThanOrderByPriceDesc(dc.getCard().getId(), 0);
            for (Product p : options) {
                if (need == 0) break;
                int take = Math.min(need, p.getStock());
                try {
                    cartService.addItem(email, p.getId(), take);
                    need -= take;
                    added += take;
                } catch (BusinessException e) {
                    // vượt tồn kho khi cộng với số đã có trong giỏ: thử bản khác
                }
            }
            if (need > 0)
                shortages.add(dc.getCard().getName() + " (" + dc.getCard().getCardCode() + ") thiếu " + need);
        }
        return new AddResult(added, shortages);
    }

    private DeckCard find(Deck deck, Long cardId) {
        return deck.getCards().stream()
                .filter(x -> x.getCard().getId().equals(cardId))
                .findFirst().orElse(null);
    }
}