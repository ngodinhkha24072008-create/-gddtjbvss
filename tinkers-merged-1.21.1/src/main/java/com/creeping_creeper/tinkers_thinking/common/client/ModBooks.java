package com.creeping_creeper.tinkers_thinking.common.client;

import com.creeping_creeper.tinkers_thinking.TinkersThinking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import modernmods.hilt.client.book.BookLoader;
import modernmods.hilt.client.book.data.BookData;
import modernmods.hilt.client.book.repository.BookRepository;
import modernmods.hilt.client.book.repository.FileRepository;
import modernmods.hilt.client.book.transformer.BookTransformer;
import modernmods.modernfoundry.library.client.book.sectiontransformer.materials.TierRangeMaterialSectionTransformer;

public class ModBooks extends BookData {
   public static final ResourceLocation FANTASTIC_GADGETRY_ID = TinkersThinking.getResource("fantastic_gadgetry");
   public static final BookData FANTASTIC_GADGETRY = BookLoader.registerBook(FANTASTIC_GADGETRY_ID, false, false, new BookRepository[0]);

   public ModBooks() {
      super(new BookRepository[0]);
   }

   public static void initBook() {
      BookLoader.registerGsonTypeAdapter(Component.class, new Serializer());
      addStandardData(FANTASTIC_GADGETRY, FANTASTIC_GADGETRY_ID);
   }

   private static void addStandardData(BookData book, ResourceLocation id) {
      book.addRepository(new FileRepository(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "book/" + id.getPath())));
      book.addTransformer(BookTransformer.indexTranformer());
      book.addTransformer(TierRangeMaterialSectionTransformer.INSTANCE);
      book.addTransformer(BookTransformer.paddingTransformer());
   }

   public static BookData getBook() {
      return FANTASTIC_GADGETRY;
   }
}
