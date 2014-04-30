SELECT
  ri.id,
  t.code,
  ri.content_type,
  replace(t.name, CHAR(9), ' ')                                                                             name,
  replace(t.artist, CHAR(9), ' ')                                                                           artist,
  replace(t.composer, CHAR(9), ' ')                                                                         composer,

  IF(c.right_type = 1, 'AUTHOR', 'RELATED'),
  p.name platform,
  c.name catalog,

  t.shareMobile,
  c.royalty                                                                                                 cat_royalty,
  IF(c.right_type = 1, cm.authorRoyalty, cm.relatedRoyalty) cm_royalty,

  ri.price,
  sum(ri.qty)                                                                                               totalQty,
  (ri.price * sum(ri.qty))                                                                                  vol,

  round((sum(ri.qty) * ri.price * (t.shareMobile / 100) * (IF(c.right_type = 1, cm.authorRoyalty, cm.relatedRoyalty) / 100) * (c.royalty / 100)), 3) revenue
FROM customer_report_item ri
  LEFT JOIN customer_report r
    ON r.id = ri.report_id
  LEFT JOIN customer cm
    ON cm.id = r.customer_id
  LEFT JOIN composition t
    ON t.id = ri.composition_id
  LEFT JOIN catalog c
    ON t.catalog_id = c.id
  LEFT JOIN platform p
    ON c.platform_id = p.id
WHERE (r.start_date BETWEEN '2014-01-01' AND '2014-04-01')
      AND ri.detected = 1
      AND r.accepted = 1
#   AND p.name = '1mp'

GROUP BY t.id;