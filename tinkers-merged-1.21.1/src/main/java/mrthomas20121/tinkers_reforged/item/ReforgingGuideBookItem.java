package mrthomas20121.tinkers_reforged.item;

import mrthomas20121.tinkers_reforged.client.TinkersReforgedBook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import modernmods.hilt.client.book.BookScreenOpener;
import modernmods.hilt.item.AbstractBookItem;

public class ReforgingGuideBookItem extends AbstractBookItem {
   public ReforgingGuideBookItem() {
      super(new Properties().stacksTo(1));
   }

   public BookScreenOpener getBook(ItemStack stack) {
      return TinkersReforgedBook.BOOK;
   }
}
