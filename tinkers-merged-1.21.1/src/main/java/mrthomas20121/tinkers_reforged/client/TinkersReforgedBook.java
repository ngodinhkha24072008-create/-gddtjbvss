package mrthomas20121.tinkers_reforged.client;

import mrthomas20121.tinkers_reforged.TinkersReforged;
import modernmods.hilt.client.book.BookLoader;
import modernmods.hilt.client.book.data.BookData;
import modernmods.hilt.client.book.repository.BookRepository;
import modernmods.hilt.client.book.repository.FileRepository;
import modernmods.hilt.client.book.transformer.BookTransformer;
import modernmods.modernfoundry.library.client.book.sectiontransformer.ModifierTagInjectorTransformer;
import modernmods.modernfoundry.library.client.book.sectiontransformer.ToolTagInjectorTransformer;
import modernmods.modernfoundry.library.client.book.sectiontransformer.materials.TierRangeMaterialSectionTransformer;

public class TinkersReforgedBook {
   public static final BookData BOOK = BookLoader.registerBook(TinkersReforged.getResource("reforging_guide"), false, false, new BookRepository[0]);

   public static void initBook() {
      addBookData(BOOK);
   }

   private static void addBookData(BookData book) {
      book.addTransformer(ToolTagInjectorTransformer.INSTANCE);
      book.addTransformer(ModifierTagInjectorTransformer.INSTANCE);
      book.addRepository(new FileRepository(TinkersReforged.getResource("book/reforging_guide")));
      book.addTransformer(TierRangeMaterialSectionTransformer.INSTANCE);
      book.addTransformer(BookTransformer.indexTranformer());
      book.addTransformer(BookTransformer.paddingTransformer());
   }
}
